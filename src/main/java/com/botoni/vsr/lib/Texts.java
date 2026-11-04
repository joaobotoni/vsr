package com.botoni.vsr.lib;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Texto livre que vai para o banco, sem espaços nas pontas como exige {@code documento.texto_valido}.
 * Texto vazio vira {@code null}.
 */
public final class Texts {

    /** Uma linha: recusa HTML e caracteres de controle ou invisíveis. */
    public static final String SAFE = "^[^\\p{Cc}\\p{Cf}<>]*$";

    /** Várias linhas (observações): como {@link #SAFE}, mas aceita quebra de linha e tabulação. */
    public static final String NOTE = "^[^\\p{Cf}<>\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F-\\x9F]*$";

    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final Pattern BREAKS = Pattern.compile("\\r\\n?");
    private static final String SPACE = " ";
    private static final String BREAK = "\n";

    private Texts() {
    }

    /** Uma linha: acentos compostos (NFC) e espaços repetidos reduzidos a um. */
    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        return blank(SPACES.matcher(Normalizer.normalize(value, Normalizer.Form.NFC).strip()).replaceAll(SPACE));
    }

    /** Várias linhas: acentos compostos (NFC) e quebras de linha no formato {@code \n}; o texto interno fica como veio. */
    public static String note(String value) {
        if (value == null) {
            return null;
        }
        return blank(BREAKS.matcher(Normalizer.normalize(value, Normalizer.Form.NFC)).replaceAll(BREAK).strip());
    }

    private static String blank(String value) {
        return value.isEmpty() ? null : value;
    }
}
