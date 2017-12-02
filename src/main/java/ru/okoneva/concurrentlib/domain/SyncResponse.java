package ru.okoneva.concurrentlib.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SyncResponse {
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

    public static SyncResponse ok() {
        return new SyncResponse(Result.OK, null);
    }

    public static SyncResponse error(final String message) {
        return new SyncResponse(Result.ERROR, message);
    }
}
