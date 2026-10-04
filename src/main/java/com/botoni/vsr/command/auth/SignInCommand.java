package com.botoni.vsr.command.auth;

import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.dto.request.auth.LoginRequest;

public record SignInCommand(LoginRequest login, SessionCommand session) {
}
