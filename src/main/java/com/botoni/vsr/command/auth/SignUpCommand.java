package com.botoni.vsr.command.auth;

import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.dto.request.auth.RegisterRequest;

public record SignUpCommand(RegisterRequest register, SessionCommand session) {
}
