package ru.okoneva.concurrentlib.domain;

public class Response {
    private Type type;
    private Object content;


    public static enum Type {
        T1("User list");

        private String label;

        Type(final String label) {
            this.label = label;
        }
    }
}
