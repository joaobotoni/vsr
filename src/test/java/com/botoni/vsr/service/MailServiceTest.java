package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.MailException;
import com.botoni.vsr.exception.custom.SenderException;
import com.botoni.vsr.exception.enums.problem.MailProblem;
import com.botoni.vsr.exception.enums.problem.SenderProblem;
import com.botoni.vsr.properties.EmailProperties;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Mail;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("Envio de e-mail")
class MailServiceTest {

    private static final Mail MAIL = new Mail(Email.of("ana@vsr.com"), "Relatório", "Segue anexo");

    private final JavaMailSender mailSender = mock(JavaMailSender.class);

    @Test
    @Controle
    @DisplayName("anexo dentro do diretório configurado é enviado")
    void attachmentInsideDirectoryIsSent(@TempDir Path root) throws Exception {
        Path uploads = Files.createDirectories(root.resolve("uploads"));
        Path report = Files.writeString(uploads.resolve("relatorio.txt"), "conteúdo");

        service(uploads).attach(MAIL, report);

        Part attachment = sentMessage().getBodyPart(1);
        assertThat(attachment.getFileName()).isEqualTo("relatorio.txt");
    }

    @Test
    @Controle
    @DisplayName("caminho com ../ para fora do diretório de anexos é recusado")
    void traversalIsRejected(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("segredo.txt"), "conteúdo sensível");
        Path uploads = Files.createDirectories(root.resolve("uploads"));
        Path traversal = uploads.resolve("../segredo.txt");

        assertThatThrownBy(() -> service(uploads).attach(MAIL, traversal))
                .isInstanceOf(SenderException.class)
                .extracting("problem").isEqualTo(SenderProblem.ATTACHMENT_OUTSIDE_DIRECTORY);
        verify(mailSender, never()).send(any(MimeMessagePreparator.class));
    }

    @Test
    @Controle
    @DisplayName("link simbólico que aponta para fora do diretório de anexos é recusado")
    void symlinkOutsideIsRejected(@TempDir Path root) throws Exception {
        Path secret = Files.writeString(root.resolve("segredo.txt"), "conteúdo sensível");
        Path uploads = Files.createDirectories(root.resolve("uploads"));
        Path link = uploads.resolve("relatorio.txt");
        try {
            Files.createSymbolicLink(link, secret);
        } catch (IOException | UnsupportedOperationException exception) {
            Assumptions.abort("sistema sem permissão para criar link simbólico");
        }

        assertThatThrownBy(() -> service(uploads).attach(MAIL, link))
                .isInstanceOf(SenderException.class)
                .extracting("problem").isEqualTo(SenderProblem.ATTACHMENT_OUTSIDE_DIRECTORY);
        verify(mailSender, never()).send(any(MimeMessagePreparator.class));
    }

    @Test
    @Controle
    @DisplayName("anexo inexistente é recusado sem enviar nada")
    void missingAttachmentIsRejected(@TempDir Path root) {
        assertThatThrownBy(() -> service(root).attach(MAIL, root.resolve("nao-existe.txt")))
                .isInstanceOf(SenderException.class)
                .extracting("problem").isEqualTo(SenderProblem.ATTACHMENT_NOT_FOUND);
        verifyNoInteractions(mailSender);
    }

    @Test
    @Controle
    @DisplayName("falha do servidor de e-mail vira erro padronizado de envio")
    void deliveryFailureIsTranslated(@TempDir Path root) {
        doThrow(new MailSendException("smtp fora do ar")).when(mailSender).send(any(MimeMessagePreparator.class));

        assertThatThrownBy(() -> service(root).send(MAIL))
                .isInstanceOf(SenderException.class)
                .extracting("problem").isEqualTo(SenderProblem.DELIVERY_FAILED);
    }

    @Test
    @Controle
    @DisplayName("e-mail ausente é recusado no envio simples e no envio com anexo, sem enviar nada")
    void missingMailIsRejected(@TempDir Path root) throws Exception {
        Path report = Files.writeString(root.resolve("relatorio.txt"), "conteúdo");
        MailService service = service(root);

        assertThatThrownBy(() -> service.send(null))
                .isInstanceOf(SenderException.class)
                .extracting("problem").isEqualTo(SenderProblem.MISSING_MAIL);
        assertThatThrownBy(() -> service.attach(null, report))
                .isInstanceOf(SenderException.class)
                .extracting("problem").isEqualTo(SenderProblem.MISSING_MAIL);
        verifyNoInteractions(mailSender);
    }

    @Test
    @Controle
    @DisplayName("envio com anexo sem nenhum arquivo é recusado")
    void attachWithoutFilesIsRejected(@TempDir Path root) {
        assertThatThrownBy(() -> service(root).attach(MAIL))
                .isInstanceOf(SenderException.class)
                .extracting("problem").isEqualTo(SenderProblem.MISSING_ATTACHMENTS);
        verifyNoInteractions(mailSender);
    }

    @Test
    @Controle
    @DisplayName("assunto com quebra de linha é recusado")
    void subjectWithLineBreakIsRejected() {
        assertThatThrownBy(() -> new Mail(Email.of("ana@vsr.com"), "Relatório\r\nBcc: atacante@vsr.com", "Corpo"))
                .isInstanceOf(MailException.class)
                .extracting("problem").isEqualTo(MailProblem.INVALID_SUBJECT);
    }

    private MailService service(Path attachments) {
        return new MailService(new EmailProperties(Email.of("no-reply@vsr.com"), attachments), mailSender);
    }

    private MimeMultipart sentMessage() throws Exception {
        ArgumentCaptor<MimeMessagePreparator> preparator = ArgumentCaptor.forClass(MimeMessagePreparator.class);
        verify(mailSender).send(preparator.capture());
        MimeMessage message = new MimeMessage((jakarta.mail.Session) null);
        preparator.getValue().prepare(message);
        return (MimeMultipart) message.getContent();
    }
}
