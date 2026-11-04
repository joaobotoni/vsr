package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PhotoException;
import com.botoni.vsr.exception.enums.problem.PhotoProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Foto de evidência")
class PhotoTest {

    @Test
    @Controle
    @DisplayName("reconhece JPEG e PNG pela assinatura dos bytes")
    void formatComesFromSignature() {
        Photo jpeg = Photo.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDB});
        Photo png = Photo.of(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0});

        assertThat(jpeg.contentType()).isEqualTo("image/jpeg");
        assertThat(jpeg.extension()).isEqualTo(".jpg");
        assertThat(png.contentType()).isEqualTo("image/png");
        assertThat(png.size()).isEqualTo(9);
    }

    @Test
    @Controle
    @DisplayName("recusa HTML, SVG, GIF e assinaturas cortadas")
    void otherFormatsAreRejected() {
        for (String content : new String[]{"<html></html>", "<svg xmlns=\"http://www.w3.org/2000/svg\"/>", "GIF89a"}) {
            assertThatThrownBy(() -> Photo.of(content.getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(PhotoException.class)
                    .extracting("problem").isEqualTo(PhotoProblem.UNSUPPORTED);
        }
        assertThatThrownBy(() -> Photo.of(new byte[]{(byte) 0xFF, (byte) 0xD8}))
                .extracting("problem").isEqualTo(PhotoProblem.UNSUPPORTED);
    }

    @Test
    @Controle
    @DisplayName("recusa arquivo vazio")
    void emptyIsRejected() {
        assertThatThrownBy(() -> Photo.of(new byte[0]))
                .isInstanceOf(PhotoException.class)
                .extracting("problem").isEqualTo(PhotoProblem.EMPTY);
    }

    @Test
    @Controle
    @DisplayName("o conteúdo não muda por fora depois de criado")
    void contentIsCopied() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        Photo photo = Photo.of(bytes);
        bytes[0] = 0;
        photo.content()[1] = 0;

        assertThat(photo.content()).containsExactly((byte) 0xFF, (byte) 0xD8, (byte) 0xFF);
    }
}
