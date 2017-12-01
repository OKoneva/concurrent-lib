package ru.okoneva.concurrentlib.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private int id;
    private String name;

    public static User DUMMY_USER() {
        return new User(0, "Dummy");
    }
}
