package com.botoni.vsr.bundle;

import com.botoni.vsr.user.entity.User;

public record SessionOwnerBundle(User user, SessionBundle session) {
}
