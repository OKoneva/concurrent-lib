package ru.okoneva.concurrentlib.domain;

import lombok.Value;

@Value
public class AsyncResponse {
    private Object result;
    private RequestType type;

    public boolean isUser() {
        return type == RequestType.USER;
    }

    public boolean isSession() {
        return type == RequestType.SESSION;
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
