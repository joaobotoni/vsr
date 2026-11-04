package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.ItemEditRequest;
import com.botoni.vsr.dto.response.ItemResponse;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/items", version = "1")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponse> edit(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                             @RequestBody @Valid ItemEditRequest request) {
        return ResponseEntity.ok(itemService.edit(principal.user(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        itemService.remove(principal.user(), id);
        return ResponseEntity.noContent().build();
    }
}
