package com.botoni.vsr.controller;

import com.botoni.vsr.dto.request.auth.LoginRequest;
import com.botoni.vsr.dto.request.auth.RegisterRequest;
import com.botoni.vsr.dto.request.session.DeviceRequest;
import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.dto.response.LoginResponse;
import com.botoni.vsr.dto.response.RegisterResponse;
import com.botoni.vsr.mapper.auth.LoginMapper;
import com.botoni.vsr.mapper.auth.RegisterMapper;
import com.botoni.vsr.mapper.session.SessionMapper;
import com.botoni.vsr.service.auth.LoginService;
import com.botoni.vsr.service.auth.RegisterService;
import com.botoni.vsr.util.RemoteAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/auth", version = "1")
@RequiredArgsConstructor
public class AuthenticationController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final RegisterMapper registerMapper;
    private final LoginMapper loginMapper;
    private final SessionMapper sessionMapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.register(registerMapper.toSignUp(request, session(request.device(), http))));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(loginService.login(loginMapper.toSignIn(request, session(request.device(), http))));
    }

    private SessionCommand session(DeviceRequest device, HttpServletRequest http) {
        return sessionMapper.toCommand(device, RemoteAddress.of(http));
    }
}
