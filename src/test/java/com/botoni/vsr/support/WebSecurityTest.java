package com.botoni.vsr.support;

import com.botoni.vsr.controller.AuthenticationController;
import com.botoni.vsr.controller.UserController;
import com.botoni.vsr.mapper.TokenMapper;
import com.botoni.vsr.service.ChangePasswordService;
import com.botoni.vsr.service.LoginService;
import com.botoni.vsr.service.RefreshService;
import com.botoni.vsr.service.RegisterService;
import com.botoni.vsr.service.SessionService;
import com.botoni.vsr.service.LogoutService;
import com.botoni.vsr.service.ProfileService;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sobe a cadeia de segurança real (SecurityConfig, filtros, JWT e rate limit) com os serviços mockados.
 * Os mocks são obtidos no teste com {@code @Autowired}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(controllers = {AuthenticationController.class, UserController.class})
@TestPropertySource("classpath:security-test.properties")
@Import(WebSecurityTestConfiguration.class)
@MockitoBean(types = {
        RegisterService.class,
        LoginService.class,
        SessionService.class,
        RefreshService.class,
        ProfileService.class,
        LogoutService.class,
        ChangePasswordService.class,
        UserDetailsService.class,
        TokenMapper.class
})
public @interface WebSecurityTest {
}
