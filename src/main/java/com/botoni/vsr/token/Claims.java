package com.botoni.vsr.token;

import java.util.UUID;

public record Claims(UUID subject, Integer session) {
}
