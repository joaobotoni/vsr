package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.ChangePasswordRequest;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.service.AuthenticationService;
import com.botoni.vsr.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private static final String ME_PATH = "/users/me";
    private static final String PASSWORD_PATH = "/users/me/password";

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @GetMapping(ME_PATH)
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.findById(userIdOf(jwt));
    }

    @PatchMapping(PASSWORD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid ChangePasswordRequest request) {
        authenticationService.changePassword(userIdOf(jwt), request.currentPassword(), request.newPassword());
    }

    private static Integer userIdOf(Jwt jwt) {
        return Integer.valueOf(jwt.getSubject());
    }
}
