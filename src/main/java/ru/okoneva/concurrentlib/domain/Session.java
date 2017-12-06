package ru.okoneva.concurrentlib.domain;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Session {
    private int id;

    public Session() {
        this.id = (int) (Math.random() * 10 + 1);
    }

    public static Session DUMMY_SESSION() {
        return new Session(0);
    }
}
