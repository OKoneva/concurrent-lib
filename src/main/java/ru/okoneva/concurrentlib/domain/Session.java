package ru.okoneva.concurrentlib.domain;

import lombok.Value;

@Value
public class Session {
    private int id;

    public Session() {
        this.id = (int) (Math.random() * 10);
    }
}
