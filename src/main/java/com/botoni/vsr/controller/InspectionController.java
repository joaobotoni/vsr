package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.InspectionEditRequest;
import com.botoni.vsr.dto.request.InspectionQuery;
import com.botoni.vsr.dto.request.InspectionRequest;
import com.botoni.vsr.dto.request.PageQuery;
import com.botoni.vsr.dto.request.RoomRequest;
import com.botoni.vsr.dto.response.InspectionDetailResponse;
import com.botoni.vsr.dto.response.InspectionResponse;
import com.botoni.vsr.dto.response.PageResponse;
import com.botoni.vsr.dto.response.RoomResponse;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.InspectionService;
import com.botoni.vsr.service.RoomService;
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

import java.util.UUID;

@RestController
@RequestMapping(path = "/inspections", version = "1")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;
    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<InspectionResponse> start(@AuthenticationPrincipal Principal principal, @RequestBody @Valid InspectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inspectionService.start(principal.user(), request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<InspectionResponse>> list(@AuthenticationPrincipal Principal principal,
                                                                 InspectionQuery query, @Valid PageQuery page) {
        return ResponseEntity.ok(inspectionService.list(principal.user(), query, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InspectionDetailResponse> view(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(inspectionService.view(principal.user(), id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InspectionResponse> edit(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                                   @RequestBody @Valid InspectionEditRequest request) {
        return ResponseEntity.ok(inspectionService.edit(principal.user(), id, request));
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<InspectionResponse> finish(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(inspectionService.finish(principal.user(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        inspectionService.remove(principal.user(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rooms")
    public ResponseEntity<RoomResponse> add(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                            @RequestBody @Valid RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.add(principal.user(), id, request));
    }
}
