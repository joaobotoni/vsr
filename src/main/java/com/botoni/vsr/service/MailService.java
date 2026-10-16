package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.SenderException;
import com.botoni.vsr.exception.enums.problem.SenderProblem;
import com.botoni.vsr.properties.EmailProperties;
import com.botoni.vsr.vo.Attachment;
import com.botoni.vsr.vo.Mail;
import jakarta.activation.DataHandler;
import jakarta.activation.FileTypeMap;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final EmailProperties emailProperties;
    private final JavaMailSender mailSender;

    public void send(Mail mail) {
        present(mail);
        MimeMessagePreparator message = plain(mail);
        deliver(message);
    }

    public void sendAttachments(Mail mail, Path... attachments) {
        present(mail);
        attached(attachments);
        List<Attachment> files = load(attachments);
        MimeMessagePreparator message = multipart(mail, files);
        deliver(message);
    }

    private MimeMessagePreparator plain(Mail mail) {
        return message -> write(message, mail);
    }

    private void write(MimeMessage message, Mail mail) throws MessagingException {
        header(message, mail);
        message.setText(mail.body(), CHARSET);
    }

    private List<Attachment> load(Path... attachments) {
        List<Attachment> files = new ArrayList<>();
        for (Path attachment : attachments) {
            files.add(file(real(attachment)));
        }
        return files;
    }

    private Attachment file(Path real) {
        inside(real);
        return new Attachment(real.getFileName().toString(), read(real));
    }

    private MimeMessagePreparator multipart(Mail mail, List<Attachment> files) {
        return message -> compose(message, mail, files);
    }

    private void compose(MimeMessage message, Mail mail, List<Attachment> files) throws MessagingException {
        header(message, mail);
        message.setContent(parts(mail.body(), files));
    }

    private void header(MimeMessage message, Mail mail) throws MessagingException {
        message.setFrom(new InternetAddress(sender()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to(mail)));
        message.setSubject(mail.subject(), CHARSET);
    }

    private static String to(Mail mail) {
        return mail.to().value();
    }

    private String sender() {
        return emailProperties.from().value();
    }

    private Path directory() {
        return emailProperties.attachments();
    }

    private static Multipart parts(String body, List<Attachment> files) throws MessagingException {
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(body(body));
        for (Attachment file : files) {
            multipart.addBodyPart(part(file));
        }
        return multipart;
    }

    private static MimeBodyPart body(String body) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        part.setText(body, CHARSET);
        return part;
    }

    private static MimeBodyPart part(Attachment file) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        part.setDataHandler(new DataHandler(new ByteArrayDataSource(file.bytes(), type(file))));
        part.setFileName(file.name());
        return part;
    }

    private static String type(Attachment file) {
        return FileTypeMap.getDefaultFileTypeMap().getContentType(file.name());
    }

    private void present(Mail mail) {
        if (missing(mail)) {
            throw new SenderException(SenderProblem.MISSING_MAIL);
        }
    }

    private void deliver(MimeMessagePreparator message) {
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new SenderException(SenderProblem.DELIVERY_FAILED, exception);
        }
    }

    private void attached(Path... attachments) {
        if (empty(attachments)) {
            throw new SenderException(SenderProblem.MISSING_ATTACHMENTS);
        }
    }

    private void inside(Path real) {
        if (escaped(real)) {
            throw new SenderException(SenderProblem.ATTACHMENT_OUTSIDE_DIRECTORY);
        }
    }

    private static Path real(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException exception) {
            throw new SenderException(SenderProblem.ATTACHMENT_NOT_FOUND, exception);
        }
    }

    private static byte[] read(Path real) {
        try (InputStream input = Files.newInputStream(real, LinkOption.NOFOLLOW_LINKS)) {
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new SenderException(SenderProblem.ATTACHMENT_NOT_FOUND, exception);
        }
    }

    private static boolean missing(Mail mail) {
        return mail == null;
    }

    private static boolean empty(Path... attachments) {
        return attachments == null || attachments.length == 0;
    }

    private boolean escaped(Path real) {
        return !real.startsWith(real(directory()));
    }
}
