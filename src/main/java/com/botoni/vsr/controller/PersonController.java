package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.CompanyRequest;
import com.botoni.vsr.dto.request.IndividualRequest;
import com.botoni.vsr.dto.request.PageQuery;
import com.botoni.vsr.dto.request.PersonEditRequest;
import com.botoni.vsr.dto.response.PageResponse;
import com.botoni.vsr.dto.response.PersonResponse;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/people", version = "1")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping("/individuals")
    public ResponseEntity<PersonResponse> individual(@AuthenticationPrincipal Principal principal,
                                                     @RequestBody @Valid IndividualRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.individual(principal.user(), request));
    }

    @PostMapping("/companies")
    public ResponseEntity<PersonResponse> company(@AuthenticationPrincipal Principal principal,
                                                  @RequestBody @Valid CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.company(principal.user(), request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PersonResponse>> list(@AuthenticationPrincipal Principal principal, @Valid PageQuery query) {
        return ResponseEntity.ok(personService.list(principal.user(), query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> view(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(personService.view(principal.user(), id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PersonResponse> edit(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                               @RequestBody @Valid PersonEditRequest request) {
        return ResponseEntity.ok(personService.edit(principal.user(), id, request));
    }
}
