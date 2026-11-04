package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.CompanyAccountRequest;
import com.botoni.vsr.dto.request.MemberRequest;
import com.botoni.vsr.dto.response.CompanyResponse;
import com.botoni.vsr.dto.response.MemberResponse;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.CompanyService;
import com.botoni.vsr.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/companies", version = "1")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final MembershipService membershipService;

    @PostMapping
    public ResponseEntity<CompanyResponse> open(@AuthenticationPrincipal Principal principal, @RequestBody @Valid CompanyAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.open(principal.user(), request));
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> list(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(companyService.list(principal.user()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> view(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(companyService.view(principal.user(), id));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<MemberResponse>> members(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(membershipService.list(principal.user(), id));
    }

    /** Responde 204 exista ou não uma conta com o e-mail: a lista de membros mostra quem entrou. */
    @PostMapping("/{id}/members")
    public ResponseEntity<Void> link(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                     @RequestBody @Valid MemberRequest request) {
        membershipService.link(principal.user(), id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/members/{member}")
    public ResponseEntity<Void> unlink(@AuthenticationPrincipal Principal principal, @PathVariable UUID id, @PathVariable UUID member) {
        membershipService.unlink(principal.user(), id, member);
        return ResponseEntity.noContent().build();
    }
}
