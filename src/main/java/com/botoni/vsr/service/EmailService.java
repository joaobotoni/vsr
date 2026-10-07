package com.botoni.vsr.service;

import com.botoni.vsr.email.Details;
import com.botoni.vsr.email.Sender;
import com.botoni.vsr.exception.custom.SenderException;
import com.botoni.vsr.exception.enums.problem.SenderProblem;
import com.botoni.vsr.properties.EmailProperties;
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
public class EmailService implements Sender {

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final String sender;

    private final Path directory;

    private final JavaMailSender mailSender;

    public EmailService(EmailProperties emailProperties, JavaMailSender mailSender) {
        this.sender = emailProperties.from().value();
        this.directory = normalize(emailProperties.attachments());
        this.mailSender = mailSender;
    }

    @Override
    public void send(Details details) {
        if (details == null) {
            throw new SenderException(SenderProblem.MISSING_DETAILS);
        }
        SimpleMailMessage message = text(details);
        deliverText(message);
    }

    @Override
    public void send(Details details, Path... attachments) {
        if (details == null) {
            throw new SenderException(SenderProblem.MISSING_DETAILS);
        }
        if (!hasAttachments(attachments)) {
            throw new SenderException(SenderProblem.MISSING_ATTACHMENTS);
        }
        if (!areInsideDirectory(attachments)) {
            throw new SenderException(SenderProblem.ATTACHMENT_OUTSIDE_DIRECTORY);
        }
        MimeMessagePreparator message = mime(details, attachments);
        deliverMime(message);
    }

    private SimpleMailMessage text(Details details) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(to(details));
        message.setSubject(details.subject());
        message.setText(details.body());
        return message;
    }

    private void deliverText(SimpleMailMessage message) {
        mailSender.send(message);
    }

    private MimeMessagePreparator mime(Details details, Path... attachments) {
        return message -> fill(message, details, attachments);
    }

    private void fill(MimeMessage message, Details details, Path... attachments) throws MessagingException, IOException {
        message.setFrom(new InternetAddress(sender));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to(details)));
        message.setSubject(details.subject(), CHARSET);
        message.setContent(parts(details.body(), attachments));
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

    private static String to(Details details) {
        return details.to().value();
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
