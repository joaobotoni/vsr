package com.botoni.vsr.controller;

import com.botoni.vsr.dto.response.StateResponse;
import com.botoni.vsr.service.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping(path = "/states", version = "1")
@RequiredArgsConstructor
public class StateController {

    private final StateService stateService;

    /** Dado estático: o app pode guardar por um dia. */
    @GetMapping
    public ResponseEntity<List<StateResponse>> list() {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate()).body(stateService.list());
    }
}
