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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class EmailService {

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final String sender;

    private final Path directory;

    private final JavaMailSender mailSender;

    public EmailService(EmailProperties emailProperties, JavaMailSender mailSender) {
        this.sender = emailProperties.from().value();
        this.directory = normalize(emailProperties.attachments());
        this.mailSender = mailSender;
    }

    public void send(Mail mail) {
        SimpleMailMessage message = text(present(mail));
        deliverText(message);
    }

    public void attach(Mail mail, Path... attachments) {
        MimeMessagePreparator message = mime(present(mail), allowed(attachments));
        deliverMime(message);
    }

    private static Mail present(Mail mail) {
        if (mail == null) {
            throw new SenderException(SenderProblem.MISSING_MAIL);
        }
        return mail;
    }

    private Path[] allowed(Path... attachments) {
        if (!hasAttachments(attachments)) {
            throw new SenderException(SenderProblem.MISSING_ATTACHMENTS);
        }
        if (!areInsideDirectory(attachments)) {
            throw new SenderException(SenderProblem.ATTACHMENT_OUTSIDE_DIRECTORY);
        }
        return attachments;
    }

    private SimpleMailMessage text(Mail mail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(to(mail));
        message.setSubject(mail.subject());
        message.setText(mail.body());
        return message;
    }

    private void deliverText(SimpleMailMessage message) {
        mailSender.send(message);
    }

    private MimeMessagePreparator mime(Mail mail, Path... attachments) {
        return message -> fill(message, mail, attachments);
    }

    private void fill(MimeMessage message, Mail mail, Path... attachments) throws MessagingException, IOException {
        message.setFrom(new InternetAddress(sender));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to(mail)));
        message.setSubject(mail.subject(), CHARSET);
        message.setContent(parts(mail.body(), attachments));
    }

    private void deliverMime(MimeMessagePreparator message) {
        mailSender.send(message);
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

    private static String to(Mail mail) {
        return mail.to().value();
    }

    private static boolean hasAttachments(Path... attachments) {
        return attachments != null && attachments.length > 0;
    }

    private boolean areInsideDirectory(Path... attachments) {
        return Arrays.stream(attachments).allMatch(this::isInsideDirectory);
    }

    private boolean isInsideDirectory(Path attachment) {
        return normalize(attachment).startsWith(directory);
    }

    private static Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
