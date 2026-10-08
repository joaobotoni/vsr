package com.botoni.vsr.support;

import com.botoni.vsr.configuration.DenialsConfig;
import com.botoni.vsr.configuration.FilterConfig;
import com.botoni.vsr.configuration.JwtConfig;
import com.botoni.vsr.configuration.RateLimitConfig;
import com.botoni.vsr.configuration.SecurityConfig;
import com.botoni.vsr.properties.JwtProperties;
import com.botoni.vsr.properties.RateLimitProperties;
import com.botoni.vsr.properties.RequestProperties;
import com.botoni.vsr.properties.VersionProperties;
import com.botoni.vsr.service.TokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@EnableConfigurationProperties({JwtProperties.class, RateLimitProperties.class, RequestProperties.class, VersionProperties.class})
@Import({SecurityConfig.class, DenialsConfig.class, FilterConfig.class, RateLimitConfig.class, JwtConfig.class, TokenService.class})
class WebSecurityTestConfiguration {
}
