package ru.okoneva.concurrentlib.domain;

public enum RequestType {
    USER(false),
    SESSION(true);

    private boolean isQueueResponse;

    RequestType(final boolean isQueueResponse) {
        this.isQueueResponse = isQueueResponse;
    }

    public boolean isQueueResponse() {
        return isQueueResponse;
    }
}
