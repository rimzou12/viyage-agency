package com.agencyvoyage.domain.support;

import java.util.Objects;
import java.util.UUID;

public record ContactMessageId(UUID value) {

    public ContactMessageId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ContactMessageId newId() {
        return new ContactMessageId(UUID.randomUUID());
    }

    public static ContactMessageId of(String value) {
        return new ContactMessageId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
