package io.github.rieske;

import java.util.UUID;

record Message(UUID id, String payload) {
    Message {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Non blank message is required");
        }
    }
}
