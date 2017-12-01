package ru.okoneva.concurrentlib.domain;

import lombok.Value;

@Value
public class Session {
    private double id;

    public Session() {
        this.id = Math.random();
    }
}
