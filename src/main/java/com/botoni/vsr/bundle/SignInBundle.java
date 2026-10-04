package com.botoni.vsr.bundle;

import com.botoni.vsr.auth.dto.request.LoginRequest;

public record SignInBundle(LoginRequest login, SessionBundle session) {
}
