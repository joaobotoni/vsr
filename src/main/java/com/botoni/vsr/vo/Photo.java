package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.PhotoException;
import com.botoni.vsr.exception.enums.problem.PhotoProblem;

import java.util.Arrays;

/**
 * Foto de evidência. O formato vem da assinatura dos primeiros bytes (JPEG ou PNG), nunca do nome do arquivo nem do
 * {@code Content-Type} enviados pelo cliente.
 */
public record Photo(byte[] content, String contentType, String extension) {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final String JPEG_TYPE = "image/jpeg";
    private static final String PNG_TYPE = "image/png";

    public Photo {
        content = content.clone();
    }

    public static Photo of(byte[] content) {
        if (content == null || content.length == 0) {
            throw new PhotoException(PhotoProblem.EMPTY);
        }
        if (starts(content, JPEG)) {
            return new Photo(content, JPEG_TYPE, ".jpg");
        }
        if (starts(content, PNG)) {
            return new Photo(content, PNG_TYPE, ".png");
        }
        throw new PhotoException(PhotoProblem.UNSUPPORTED);
    }

    @Override
    public byte[] content() {
        return content.clone();
    }

    public int size() {
        return content.length;
    }

    private static boolean starts(byte[] content, byte[] signature) {
        return content.length >= signature.length
                && Arrays.equals(content, 0, signature.length, signature, 0, signature.length);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Photo photo && Arrays.equals(content, photo.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "Photo[" + contentType + ", " + content.length + " bytes]";
    }
}
