package ru.okoneva.concurrentlib.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.okoneva.concurrentlib.domain.Response;
import ru.okoneva.concurrentlib.domain.Session;
import ru.okoneva.concurrentlib.domain.User;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MyServiceImpl implements MyService {

    private final ExecutorService executor;

    public MyServiceImpl(@Value("${concurrentlib.thread.number}") final int nThreads) {
        log.info("Количество тредов = {}", nThreads);
        this.executor = Executors.newFixedThreadPool(nThreads);
    }

    public Response findUser(final int id, final BlockingQueue<User> queue) {
        if (id < 10) {
            startUserSearch(id, queue);
            return Response.ok();
        }
        return Response.error("Id is " + id);
    }

    @Override
    public Response generateSession(final BlockingQueue<Session> queue) {
        startSessionGeneration(queue);
        return Response.ok();
    }

    private void startUserSearch1(final int id, final BlockingQueue<User> queue) {
        new Thread(() -> {
            Callable<User> task = () -> {
                try {
                    TimeUnit.SECONDS.sleep(2);
                    return new User(id, "User_" + id);
                } catch (InterruptedException e) {
                    throw new IllegalStateException("task interrupted", e);
                }
            };
            final User user;
            try {
                user = executor.submit(task).get();
                log.info("Найден юзер {}", user);
                queue.put(user);
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        }).start();
    }

    private void startUserSearch(final int id, final BlockingQueue<User> queue) {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                final User user = new User(id, "User_" + id);
                log.info("Найден юзер {}", user);
                queue.put(user);
            } catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        }).start();
    }

    private void startSessionGeneration(final BlockingQueue<Session> queue) {
        new Thread(() -> {
            Callable<Session> task = () -> {
                try {
                    TimeUnit.SECONDS.sleep(2);
                    return new Session();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("task interrupted", e);
                }
            };
            final Session session;
            try {
                session = executor.submit(task).get();
                queue.put(session);
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        }).start();
    }
}
