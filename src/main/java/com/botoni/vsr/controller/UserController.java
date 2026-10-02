package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.ChangePasswordRequest;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.service.password.ChangePasswordService;
import com.botoni.vsr.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users", version = "1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ChangePasswordService changePasswordService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.findById(user.getId()));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user, @RequestBody @Valid ChangePasswordRequest request) {
        changePasswordService.changePassword(user.getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}