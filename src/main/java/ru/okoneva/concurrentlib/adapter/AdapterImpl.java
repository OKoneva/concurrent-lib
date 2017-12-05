package ru.okoneva.concurrentlib.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.okoneva.concurrentlib.domain.AsyncResponse;
import ru.okoneva.concurrentlib.domain.Session;
import ru.okoneva.concurrentlib.domain.SyncResponse;
import ru.okoneva.concurrentlib.domain.User;
import ru.okoneva.concurrentlib.service.MyService;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class AdapterImpl implements MyAdapter {

    private static final Long DEFAULT_TIMEOUT_MS = 2000L;

    @Autowired
    private final MyService service = null;

    private final ReentrantLock userLock = new ReentrantLock();
    private final Condition userCondition = userLock.newCondition();
    private final Queue<User> userQueue = new LinkedList<>();
    private final Object sessionMutex = new Object();
    private final Queue<Session> sessionQueue = new LinkedList<>();

    public User findUser(final int id) {
        log.info("Получена команда для юзера {}", id);
        try {
            userLock.lock();
            log.info("Блокировка поставлена {}", id);
            final SyncResponse syncResponse = service.findUser(id);
            log.info("Синхронный ответ получен {}", id);
            if (syncResponse.isError()) {
                log.error("Получена ошибка {}", syncResponse.getMessage());
                return User.DUMMY_USER();
            }
            final Long timeBefore = System.currentTimeMillis();
            while (userQueue.isEmpty() && System.currentTimeMillis() - timeBefore < DEFAULT_TIMEOUT_MS) {
                userCondition.await(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }
            final User user = userQueue.poll();
            if (user != null) {
                log.info("Юзер {} получена для {}", user, id);
            } else {
                log.error("Время ожидания юзера истекло для {}", id);
            }
            return user;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException("task interrupted", e);
        } finally {
            log.info("Блокировка снята {}", id);
            userLock.unlock();
        }
    }

    public Session generateSession(final int taskId) {
        try {
            log.info("Получена команда для создания сессии {}", taskId);
            service.generateSession();
            synchronized (sessionMutex) {
                final Long timeBefore = System.currentTimeMillis();
                while (sessionQueue.isEmpty() && System.currentTimeMillis() - timeBefore < DEFAULT_TIMEOUT_MS) {
                    log.info("Ждем сессию для {}", taskId);
                    sessionMutex.wait(DEFAULT_TIMEOUT_MS);
                }
                final Session poll = sessionQueue.poll();
                if (poll != null) {
                    log.info("Сессия получена для {}", taskId);
                } else {
                    log.error("Время ожидания сессии истекло для {}", taskId);
                }
                return poll;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException("task interrupted", e);
        }

    }

    @Override
    public void putResult(final AsyncResponse response) {
        if (response.isSession()) {
            synchronized (sessionMutex) {
                sessionQueue.add(response.getSession());
                log.info("Получен асинхронный ответ с сессией");
                sessionMutex.notify();
            }
        } else if (response.isUser()) {
            if (userLock.isLocked()) {
                userQueue.add(response.getUser());
                log.info("Получен ожидаемый асинхронный ответ с юзером {}", response.getUser());
                userCondition.signal();
            } else { //ответ пришел поздно
                log.info("Получен опоздавший асинхронный ответ с юзером {}", response.getUser());
            }

        }
    }
}
