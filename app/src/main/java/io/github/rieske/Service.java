package io.github.rieske;

import java.util.Optional;
import java.util.UUID;

class Service {
    private final Repository repository;

    Service(Repository repository) {
        this.repository = repository;
    }

    UUID addMessage(String payload) {
        var message = new Message(UUID.randomUUID(), payload);
        repository.saveMessage(message);
        return message.id();
    }

    Optional<Message> findMessage(UUID id) {
        return repository.findMessage(id);
    }

    void deleteMessage(UUID id) {
        repository.deleteMessage(id);
    }
}
