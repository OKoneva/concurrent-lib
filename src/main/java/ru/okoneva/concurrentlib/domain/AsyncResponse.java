package ru.okoneva.concurrentlib.domain;

import lombok.Value;

@Value
public class AsyncResponse {
    private Object result;
    private Type type;

    public enum Type {
        USER,
        SESSION
    }

    public boolean isUser() {
        return type == Type.USER;
    }

    public boolean isSession() {
        return type == Type.SESSION;
    }

    public User getUser() {
        if (isUser()) {
            return (User) result;
        } else {
            throw new IllegalStateException("Result type is not User");
        }
    }

    public Session getSession() {
        if (isSession()) {
            return (Session) result;
        } else {
            throw new IllegalStateException("Result type is not Session");
        }
    }
}
