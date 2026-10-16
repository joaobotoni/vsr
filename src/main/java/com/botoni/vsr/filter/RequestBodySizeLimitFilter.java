package com.botoni.vsr.filter;

import com.botoni.vsr.exception.custom.RequestException;
import com.botoni.vsr.exception.enums.problem.RequestProblem;
import com.botoni.vsr.properties.RequestProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RequestBodySizeLimitFilter extends OncePerRequestFilter {

    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";
    private final RequestProperties properties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException, ServletException {

        validate(request);
        filterChain.doFilter(bounded(request), response);
    }

    private void validate(HttpServletRequest request) {
        if (isChunked(request)) {
            throw new RequestException(RequestProblem.LENGTH_REQUIRED);
        }

        if (isOversized(request)) {
            throw new RequestException(RequestProblem.BODY_TOO_LARGE, max());
        }
    }

    private HttpServletRequest bounded(HttpServletRequest request) throws IOException {
        return declared(request) ? request : buffer(request);
    }

    private HttpServletRequest buffer(HttpServletRequest request) throws IOException {
        byte[] body = read(request);
        fits(body);
        return new BufferedRequest(request, body);
    }

    private byte[] read(HttpServletRequest request) throws IOException {
        return request.getInputStream().readNBytes(Math.toIntExact(max() + 1));
    }

    private long max() {
        return properties.maxBodySize().toBytes();
    }

    private void fits(byte[] body) {
        if (exceeds(body)) {
            throw new RequestException(RequestProblem.BODY_TOO_LARGE, max());
        }
    }

    private boolean isChunked(HttpServletRequest request) {
        String transferEncoding = request.getHeader(TRANSFER_ENCODING);
        return transferEncoding != null && transferEncoding.toLowerCase().contains(CHUNKED);
    }

    private boolean isOversized(HttpServletRequest request) {
        long contentLength = request.getContentLengthLong();
        return contentLength > max();
    }

    private static boolean declared(HttpServletRequest request) {
        return request.getContentLengthLong() >= 0;
    }

    private boolean exceeds(byte[] body) {
        return body.length > max();
    }

    private static final class BufferedRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        private BufferedRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new BufferedBody(body);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), charset()));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }

        private Charset charset() {
            String encoding = getCharacterEncoding();
            return encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        }
    }

    private static final class BufferedBody extends ServletInputStream {

        private final ByteArrayInputStream input;

        private BufferedBody(byte[] body) {
            this.input = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            return input.read();
        }

        @Override
        public int read(byte[] bytes, int offset, int length) {
            return input.read(bytes, offset, length);
        }

        @Override
        public boolean isFinished() {
            return input.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException("O corpo já foi lido por inteiro; leitura assíncrona não se aplica");
        }
    }
}
