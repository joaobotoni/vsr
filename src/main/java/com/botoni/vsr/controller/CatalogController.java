package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.ItemTypeRequest;
import com.botoni.vsr.dto.request.RoomTypeRequest;
import com.botoni.vsr.dto.response.ItemTypeResponse;
import com.botoni.vsr.dto.response.RoomTypeResponse;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/catalog", version = "1")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomTypeResponse>> rooms(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(catalogService.rooms(principal.user()));
    }

    @PostMapping("/rooms")
    public ResponseEntity<RoomTypeResponse> addRoom(@AuthenticationPrincipal Principal principal,
                                                    @RequestBody @Valid RoomTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addRoom(principal.user(), request));
    }

    @PostMapping("/items")
    public ResponseEntity<ItemTypeResponse> addItem(@AuthenticationPrincipal Principal principal,
                                                    @RequestBody @Valid ItemTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addItem(principal.user(), request));
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemTypeResponse>> items(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(catalogService.items(principal.user()));
    }
}
