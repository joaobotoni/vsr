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
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class EmailService implements Sender {

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final EmailProperties emailProperties;
    private final JavaMailSender mailSender;

    @Override
    public void send(Details details) {
        requireDetails(details);
        mailSender.send(message(details));
    }

    @Override
    public void send(Details details, Path... attachments) {
        requireDetails(details);
        requireAttachments(attachments);
        mailSender.send(mime -> write(mime, details, attachments));
    }

    private String from() {
        return emailProperties.from().value();
    }

    private SimpleMailMessage message(Details details) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from());
        message.setTo(details.to().value());
        message.setSubject(details.subject());
        message.setText(details.body());
        return message;
    }

    private void write(MimeMessage mime, Details details, Path... attachments) throws MessagingException, IOException {
        mime.setFrom(new InternetAddress(from()));
        mime.setRecipient(Message.RecipientType.TO, new InternetAddress(details.to().value()));
        mime.setSubject(details.subject(), CHARSET);
        mime.setContent(content(details.body(), attachments));
    }

    private static void requireDetails(Details details) {
        if (details == null) {
            throw new SenderException(SenderProblem.MISSING_DETAILS);
        }
    }

    private static void requireAttachments(Path... attachments) {
        if (!hasAttachments(attachments)) {
            throw new SenderException(SenderProblem.MISSING_ATTACHMENTS);
        }
    }

    private static boolean hasAttachments(Path... attachments) {
        return attachments != null && attachments.length > 0;
    }

    private static Multipart content(String body, Path... attachments) throws MessagingException, IOException {
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(text(body));
        for (Path path : attachments) {
            multipart.addBodyPart(file(path));
        }
        return multipart;
    }

    private static MimeBodyPart text(String body) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        part.setText(body, CHARSET);
        return part;
    }

    private static MimeBodyPart file(Path path) throws MessagingException, IOException {
        MimeBodyPart part = new MimeBodyPart();
        part.attachFile(path.toFile());
        return part;
    }
}
