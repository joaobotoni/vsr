package com.botoni.vsr.user.controller;

import com.botoni.vsr.user.dto.response.UserResponse;
import com.botoni.vsr.infra.security.Principal;
import com.botoni.vsr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users", version = "1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(userService.findById(principal.user().getId()));
    }
}