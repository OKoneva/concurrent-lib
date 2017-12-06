package ru.okoneva.concurrentlib.domain;

import lombok.Data;

import java.util.concurrent.locks.Condition;

@Data
public class ResponseHolder {
    private final Condition responseCondition;
    private Object value;

    public ResponseHolder(final Condition responseCondition) {
        this.responseCondition = responseCondition;
    }

    public boolean isEmpty() {
        return value == null;
    }
    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
