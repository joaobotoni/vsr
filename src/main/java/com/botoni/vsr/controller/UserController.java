package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.ChangePasswordRequest;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.network.ClientNetwork;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.ChangePasswordService;
import com.botoni.vsr.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
@RequestMapping(path = "/users", version = "1")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;
    private final ChangePasswordService changePasswordService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(profileService.view(principal.user()));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<TokenResponse> change(@AuthenticationPrincipal Principal principal, @RequestBody @Valid ChangePasswordRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(changePasswordService.change(principal.user(), principal.session(),
                request.currentPassword(), request.newPassword().password(), ip(http)));
    }

    private static InetAddress ip(HttpServletRequest http) {
        return ClientNetwork.address(http.getRemoteAddr());
    }
}
