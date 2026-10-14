package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.SenderException;
import com.botoni.vsr.exception.enums.problem.SenderProblem;
import com.botoni.vsr.properties.EmailProperties;
import com.botoni.vsr.vo.Mail;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

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
        contained(attachments);
        MimeMessagePreparator message = multipart(mail, attachments);
        deliver(message);
    }

    private MimeMessagePreparator plain(Mail mail) {
        return message -> write(message, mail);
    }

    private void write(MimeMessage message, Mail mail) throws MessagingException {
        header(message, mail);
        message.setText(mail.body(), CHARSET);
    }

    private MimeMessagePreparator multipart(Mail mail, Path... attachments) {
        return message -> compose(message, mail, attachments);
    }

    private void compose(MimeMessage message, Mail mail, Path... attachments) throws MessagingException, IOException {
        header(message, mail);
        message.setContent(parts(mail.body(), attachments));
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

    private static Multipart parts(String body, Path... attachments) throws MessagingException, IOException {
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(body(body));
        for (Path attachment : attachments) {
            multipart.addBodyPart(file(attachment));
        }
        return multipart;
    }

    private static MimeBodyPart body(String body) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        part.setText(body, CHARSET);
        return part;
    }

    private static MimeBodyPart file(Path attachment) throws MessagingException, IOException {
        MimeBodyPart part = new MimeBodyPart();
        part.attachFile(attachment.toFile());
        return part;
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

    private void contained(Path... attachments) {
        for (Path attachment : attachments) {
            inside(attachment);
        }
    }

    private void inside(Path attachment) {
        if (escaped(attachment)) {
            throw new SenderException(SenderProblem.ATTACHMENT_OUTSIDE_DIRECTORY);
        }
    }

    private static boolean missing(Mail mail) {
        return mail == null;
    }

    private static boolean empty(Path... attachments) {
        return attachments == null || attachments.length == 0;
    }

    private boolean escaped(Path attachment) {
        return !real(attachment).startsWith(real(directory()));
    }

    private static Path real(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException exception) {
            throw new SenderException(SenderProblem.ATTACHMENT_NOT_FOUND, exception);
        }
    }
}