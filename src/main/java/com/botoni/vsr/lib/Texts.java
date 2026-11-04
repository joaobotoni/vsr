package com.botoni.vsr.lib;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Texto livre que vai para o banco: acentos compostos (NFC), sem espaços nas pontas e com espaços repetidos reduzidos
 * a um, como exige {@code documento.texto_valido}. Texto vazio vira {@code null}.
 */
public final class Texts {

    /** Recusa HTML e caracteres de controle ou invisíveis. */
    public static final String SAFE = "^[^\\p{Cc}\\p{Cf}<>]*$";

    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final String SPACE = " ";

    private Texts() {
    }

    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = SPACES.matcher(Normalizer.normalize(value, Normalizer.Form.NFC).strip()).replaceAll(SPACE);
        return cleaned.isEmpty() ? null : cleaned;
    }
}
