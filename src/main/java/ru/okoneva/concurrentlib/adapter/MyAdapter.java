package ru.okoneva.concurrentlib.adapter;

import ru.okoneva.concurrentlib.domain.User;

import java.util.List;

public interface MyAdapter {

    List<User> findUsers();
}
