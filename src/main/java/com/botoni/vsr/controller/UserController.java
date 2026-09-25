package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.ChangePasswordRequest;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.entity.User;
import com.botoni.vsr.service.AuthenticationService;
import com.botoni.vsr.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users", version = "1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return userService.findById(user.getId());
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal User user, @RequestBody @Valid ChangePasswordRequest request) {
        authenticationService.changePassword(user.getId(), request.currentPassword(), request.newPassword());
    }
}
