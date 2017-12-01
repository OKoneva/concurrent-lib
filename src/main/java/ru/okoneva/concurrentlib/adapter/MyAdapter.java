package ru.okoneva.concurrentlib.adapter;

import ru.okoneva.concurrentlib.domain.Session;
import ru.okoneva.concurrentlib.domain.User;

public interface MyAdapter {

    User findUser(int id);

    Session generateSession(int taskId);
}
