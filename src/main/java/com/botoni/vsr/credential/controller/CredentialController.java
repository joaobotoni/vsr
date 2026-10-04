package com.botoni.vsr.credential.controller;

import com.botoni.vsr.credential.dto.request.ChangePasswordRequest;
import com.botoni.vsr.credential.service.ChangePasswordService;
import com.botoni.vsr.infra.security.Principal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users", version = "1")
@RequiredArgsConstructor
public class CredentialController {

    private final ChangePasswordService changePasswordService;

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Principal principal, @RequestBody @Valid ChangePasswordRequest request) {
        changePasswordService.changePassword(principal.user().getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
