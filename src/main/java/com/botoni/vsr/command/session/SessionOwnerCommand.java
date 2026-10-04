package com.botoni.vsr.command.session;

import com.botoni.vsr.entity.users.User;

public record SessionOwnerCommand(User user, SessionCommand session) {
}
