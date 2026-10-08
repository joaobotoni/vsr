package com.botoni.vsr.filter;

import com.botoni.vsr.exception.custom.RequestException;
import com.botoni.vsr.exception.enums.problem.RequestProblem;
import com.botoni.vsr.properties.RequestProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RequestBodySizeLimitFilter extends OncePerRequestFilter {

    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";
    private final RequestProperties properties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        validateRequestBodyLimits(request);
        filterChain.doFilter(request, response);
    }

    private void validateRequestBodyLimits(HttpServletRequest request) {
        if (isChunked(request)) {
            throw new RequestException(RequestProblem.LENGTH_REQUIRED);
        }

        if (isExceedingSizeLimit(request)) {
            throw new RequestException(RequestProblem.BODY_TOO_LARGE, maxAllowedSize());
        }
    }

    private boolean isChunked(HttpServletRequest request) {
        String transferEncoding = request.getHeader(TRANSFER_ENCODING);
        return transferEncoding != null && transferEncoding.toLowerCase().contains(CHUNKED);
    }

    private boolean isExceedingSizeLimit(HttpServletRequest request) {
        long contentLength = request.getContentLengthLong();
        return contentLength > maxAllowedSize();
    }

    private long maxAllowedSize() {
        return properties.maxBodySize().toBytes();
    }
}
