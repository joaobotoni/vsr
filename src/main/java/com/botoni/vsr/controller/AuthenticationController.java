package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.LoginRequest;
import com.botoni.vsr.dto.request.SignupRequest;
import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private static final String SIGNUP_PATH = "/auth/signup";
    private static final String LOGIN_PATH = "/auth/login";

    private final AuthenticationService authenticationService;

    @PostMapping(SIGNUP_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@RequestBody @Valid SignupRequest request) {
        return authenticationService.signup(request);
    }

    @PostMapping(LOGIN_PATH)
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authenticationService.login(request);
    }
}
