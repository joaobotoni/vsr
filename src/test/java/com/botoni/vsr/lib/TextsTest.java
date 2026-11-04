package com.botoni.vsr.lib;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Normalização de texto livre")
class TextsTest {

    @Test
    @Controle
    @DisplayName("uma linha: espaços repetidos viram um, sem espaços nas pontas; vazio vira nulo")
    void cleanCollapsesSpaces() {
        assertThat(Texts.clean("  Rua   das\tFlores ")).isEqualTo("Rua das Flores");
        assertThat(Texts.clean("   ")).isNull();
        assertThat(Texts.clean(null)).isNull();
    }

    @Test
    @Controle
    @DisplayName("várias linhas: mantém as quebras, no formato \\n, e só apara as pontas")
    void noteKeepsLineBreaks() {
        assertThat(Texts.note("  Linha 1\r\nLinha  2\rLinha 3  ")).isEqualTo("Linha 1\nLinha  2\nLinha 3");
        assertThat(Texts.note("\n \n")).isNull();
    }

    @Test
    @Controle
    @DisplayName("observações aceitam quebra de linha, mas recusam HTML e caracteres de controle ou invisíveis")
    void notePatternRejectsUnsafeText() {
        Pattern note = Pattern.compile(Texts.NOTE);

        assertThat(note.matcher("Parede trincada\nPintura descascando\tlado esquerdo").matches()).isTrue();
        assertThat(note.matcher("<img src=x>").matches()).isFalse();
        assertThat(note.matcher("texto\u0000nulo").matches()).isFalse();
        assertThat(note.matcher("invisível​").matches()).isFalse();
        assertThat(note.matcher("controle\u0085").matches()).isFalse();
    }
}
