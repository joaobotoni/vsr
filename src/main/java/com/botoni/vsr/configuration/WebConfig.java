package com.botoni.vsr.configuration;

import com.botoni.vsr.properties.VersionProperties;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private static final String PREFIX = "/api/{version}";

    private final VersionProperties versionProperties;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(PREFIX, HandlerTypePredicate.forAnnotation(RestController.class));
    }

    @Override
    public void configureApiVersioning(@NonNull ApiVersionConfigurer configurer) {
        header(configurer);
        supported(configurer);
        fallback(configurer);
    }

    private void header(ApiVersionConfigurer configurer) {
        configurer.useRequestHeader(versionProperties.header());
    }

    private void supported(ApiVersionConfigurer configurer) {
        configurer.addSupportedVersions(versionProperties.supported());
    }

    private void fallback(ApiVersionConfigurer configurer) {
        configurer.setDefaultVersion(versionProperties.fallback());
    }
}
