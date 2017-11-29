package ru.okoneva.concurrentlib.service;

import ru.okoneva.concurrentlib.domain.User;

import java.util.List;
import java.util.concurrent.Future;

public interface MyService {
    Future<List<User>> findUser();
}
