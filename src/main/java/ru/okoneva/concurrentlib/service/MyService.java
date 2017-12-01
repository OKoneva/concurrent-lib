package ru.okoneva.concurrentlib.service;

import ru.okoneva.concurrentlib.domain.Response;
import ru.okoneva.concurrentlib.domain.Session;
import ru.okoneva.concurrentlib.domain.User;

import java.util.concurrent.BlockingQueue;

public interface MyService {

    Response findUser(int id, BlockingQueue<User> queue);

    Response generateSession(BlockingQueue<Session> queue);
}
