package ru.okoneva.concurrentlib.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Response {
    private Result result;
    @Getter
    private String message;

public boolean isOk() {
    return result == Result.OK;
}

public boolean isError() {
    return result == Result.ERROR;
}
    public enum Result {
        OK,
        ERROR
    }

    public static Response ok() {
        return new Response(Result.OK, null);
    }

    public static Response error(final String message) {
        return new Response(Result.ERROR, message);
    }
}
