package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.OwnerRequest;
import com.botoni.vsr.dto.request.PageQuery;
import com.botoni.vsr.dto.request.PropertyEditRequest;
import com.botoni.vsr.dto.request.PropertyRequest;
import com.botoni.vsr.dto.response.PageResponse;
import com.botoni.vsr.dto.response.PersonSummaryResponse;
import com.botoni.vsr.dto.response.PropertyResponse;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.OwnerService;
import com.botoni.vsr.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/properties", version = "1")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final OwnerService ownerService;

    @PostMapping
    public ResponseEntity<PropertyResponse> add(@AuthenticationPrincipal Principal principal, @RequestBody @Valid PropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.add(principal.user(), request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PropertyResponse>> list(@AuthenticationPrincipal Principal principal, @Valid PageQuery query) {
        return ResponseEntity.ok(propertyService.list(principal.user(), query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> view(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.view(principal.user(), id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PropertyResponse> edit(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                                 @RequestBody @Valid PropertyEditRequest request) {
        return ResponseEntity.ok(propertyService.edit(principal.user(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        propertyService.remove(principal.user(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/owners")
    public ResponseEntity<List<PersonSummaryResponse>> owners(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(ownerService.list(principal.user(), id));
    }

    @PostMapping("/{id}/owners")
    public ResponseEntity<Void> link(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                     @RequestBody @Valid OwnerRequest request) {
        ownerService.link(principal.user(), id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/owners/{person}")
    public ResponseEntity<Void> unlink(@AuthenticationPrincipal Principal principal, @PathVariable UUID id, @PathVariable UUID person) {
        ownerService.unlink(principal.user(), id, person);
        return ResponseEntity.noContent().build();
    }
}
