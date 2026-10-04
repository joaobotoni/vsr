package com.botoni.vsr.command.session;

import com.botoni.vsr.security.Principal;

public record AccessCommand(Principal principal, SessionCommand session) {
}
