package com.botoni.vsr.bundle;

import com.botoni.vsr.auth.dto.request.RegisterRequest;

public record SignUpBundle(RegisterRequest register, SessionBundle session) {
}
