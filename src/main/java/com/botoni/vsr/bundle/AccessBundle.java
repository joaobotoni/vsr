package com.botoni.vsr.bundle;

import com.botoni.vsr.infra.security.Principal;

public record AccessBundle(Principal principal, SessionBundle session) {
}
