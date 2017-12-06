package ru.okoneva.concurrentlib.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.okoneva.concurrentlib.adapter.MyAdapter;
import ru.okoneva.concurrentlib.domain.AsyncResponse;
import ru.okoneva.concurrentlib.domain.RequestType;
import ru.okoneva.concurrentlib.domain.Session;
import ru.okoneva.concurrentlib.domain.SyncResponse;
import ru.okoneva.concurrentlib.domain.User;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MyServiceImpl implements MyService {

    @Autowired
    private final MyAdapter adapter = null;

    private final ExecutorService executor;

    public MyServiceImpl(@Value("${concurrentlib.thread.number}") final int nThreads) {
        log.info("Количество тредов = {}", nThreads);
        this.executor = Executors.newFixedThreadPool(nThreads);
    }

    public SyncResponse findUser(final int id) {
        if (id < 10) {
            startUserSearch(id);
            return SyncResponse.ok();
        }
        return SyncResponse.error("Id is " + id);
    }

    @Override
    public SyncResponse generateSession() {
        startSessionGeneration();
        return SyncResponse.ok();
    }

    private void startUserSearch(final int id) {
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
                final User user = new User(id, "User_" + id);
                log.info("Найден юзер {}", user);
                adapter.putResult(new AsyncResponse(user, RequestType.USER));
            } catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        }).start();
    }

    private void startSessionGeneration() {
        new Thread(() -> {
            Callable<Session> task = () -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(5);
                    return new Session();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("task interrupted", e);
                }
            };
            final Session session;
            try {
                session = executor.submit(task).get();
                adapter.putResult(new AsyncResponse(session, RequestType.SESSION));
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        }).start();
    }

}
