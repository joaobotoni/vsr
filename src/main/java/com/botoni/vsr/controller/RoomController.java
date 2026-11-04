package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.ItemRequest;
import com.botoni.vsr.dto.request.RoomEditRequest;
import com.botoni.vsr.dto.response.ItemResponse;
import com.botoni.vsr.dto.response.RoomResponse;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.ItemService;
import com.botoni.vsr.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/rooms", version = "1")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final ItemService itemService;

    @PatchMapping("/{id}")
    public ResponseEntity<RoomResponse> edit(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                             @RequestBody @Valid RoomEditRequest request) {
        return ResponseEntity.ok(roomService.edit(principal.user(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        roomService.remove(principal.user(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ItemResponse> add(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                            @RequestBody @Valid ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.add(principal.user(), id, request));
    }
}
