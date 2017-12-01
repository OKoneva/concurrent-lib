package ru.okoneva.concurrentlib.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.okoneva.concurrentlib.domain.Response;
import ru.okoneva.concurrentlib.domain.Session;
import ru.okoneva.concurrentlib.domain.User;
import ru.okoneva.concurrentlib.service.MyService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Slf4j
public class AdapterImpl implements MyAdapter {

    private final MyService service;

    private final BlockingQueue<Object> userQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Session> sessionQueue = new ArrayBlockingQueue<>(1024);

    public User findUser(final int id) {
        try {
            log.info("Получена команда для юзера {}", id);
            userQueue.put(id);
            log.info("Блокировка поставлена {}", id);
            final BlockingQueue<User> result = new ArrayBlockingQueue<>(1);
            final Response response = service.findUser(id, result);
            log.info("Синхронный ответ получен {}", id);
            if (response.isError()) {
                log.error("Получена ошибка {}", response.getMessage());
                return User.DUMMY_USER();
            }
            final User user = result.take();
            log.info("Асинхронный ответ получен {}", user);
            return user;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException("task interrupted", e);
        } finally {
            log.info("Блокировка снята {}", id);
            userQueue.remove(id);
        }
    }

    @Override
    public Session generateSession(final int taskId) {
        try {
            log.info("Получена команда для создания сессии {}", taskId);
            final Response response = service.generateSession(sessionQueue);
            final Session session = sessionQueue.take();
            log.info("Сессия получена для {}", taskId);
            return session;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException("task interrupted", e);
        }
    }
}
