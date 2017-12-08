package ru.okoneva.concurrentlib.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.okoneva.concurrentlib.domain.AsyncResponse;
import ru.okoneva.concurrentlib.domain.RequestType;
import ru.okoneva.concurrentlib.domain.ResponseHolder;
import ru.okoneva.concurrentlib.domain.Session;
import ru.okoneva.concurrentlib.domain.SyncResponse;
import ru.okoneva.concurrentlib.domain.User;
import ru.okoneva.concurrentlib.service.MyService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static ru.okoneva.concurrentlib.domain.RequestType.SESSION;
import static ru.okoneva.concurrentlib.domain.RequestType.USER;

@Service
@Slf4j
public class AdapterImpl implements MyAdapter {

    private static final Long DEFAULT_TIMEOUT_MS = 500L;

    @Autowired
    private final MyService service = null;

    private final Map<RequestType, ReentrantLock> methodLocks = new ConcurrentHashMap<>();
    private final Map<RequestType, ReentrantLock> responseLocks = new ConcurrentHashMap<>();

    private final Map<RequestType, ResponseHolder> objectHolderMap = new ConcurrentHashMap<>();
    //Очереди с холдерами, ожидающими ответа. Отсюда можно удалить пустой холдер при наступлении таймаута
    private final Map<RequestType, Queue<ResponseHolder>> emptyHolderQueue = new ConcurrentHashMap<>();
    //Очереди с холдерами, содержащими ответ
    private final Map<RequestType, Queue<ResponseHolder>> fullHolderQueue = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("PostConstruct");
        methodLocks.put(USER, new ReentrantLock());

        responseLocks.put(USER, new ReentrantLock());
        responseLocks.put(SESSION, new ReentrantLock());

        emptyHolderQueue.put(SESSION, new ConcurrentLinkedQueue<>());
        fullHolderQueue.put(SESSION, new ConcurrentLinkedQueue<>());
    }

    public User findUser(final int id) {
        log.info("Получена команда для юзера {}", id);
        final ReentrantLock userLock = methodLocks.get(USER);
        final ReentrantLock userResponseLock = responseLocks.get(USER);
        userLock.lock();
        try {
            log.info("Блокировка поставлена {}", id);
            final SyncResponse syncResponse = service.findUser(id);
            log.info("Синхронный ответ получен {}", id);
            if (syncResponse.isError()) {
                log.error("Получена ошибка {}", syncResponse.getMessage());
                return User.DUMMY_USER();
            }
            final Condition userResponseCondition = userResponseLock.newCondition();
            final ResponseHolder holder = new ResponseHolder(userResponseCondition);
            objectHolderMap.put(USER, holder);
            final Long timeBefore = System.currentTimeMillis();
            userResponseLock.lock();
            while (holder.isEmpty() && System.currentTimeMillis() - timeBefore < DEFAULT_TIMEOUT_MS) {
                userResponseCondition.await(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }
            final Object user = holder.getValue();
            if (user != null) {
                log.info("Юзер {} получена для {}", user, id);
                return (User) user;
            } else {
                log.error("Время ожидания юзера истекло для {}", id);
                return null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException("task interrupted", e);
        } finally {
            objectHolderMap.remove(USER);
            if (userResponseLock.isLocked()) userResponseLock.unlock();
            userLock.unlock();
            log.info("Блокировка снята {}", id);
        }
    }

    public Session generateSession(final int taskId) {
        final ReentrantLock responseLock = responseLocks.get(SESSION);
        responseLock.lock();
        try {
            log.info("Получена команда для создания сессии {}", taskId);
            final Queue<ResponseHolder> emptyHolders = emptyHolderQueue.get(SESSION);
            final Condition sessionResponseCondition = responseLock.newCondition();
            emptyHolders.add(new ResponseHolder(sessionResponseCondition));
            service.generateSession();
            final Long timeBefore = System.currentTimeMillis();
            while (fullHolderQueue.get(SESSION).isEmpty() && System.currentTimeMillis() - timeBefore < DEFAULT_TIMEOUT_MS) {
                log.info("Ждем сессию для {}", taskId);
                sessionResponseCondition.await(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }
            final ResponseHolder response = fullHolderQueue.get(SESSION).poll();
            if (response != null) {
                log.info("Сессия получена для {}", taskId);
                return (Session) response.getValue();
            } else {
                log.error("Время ожидания сессии истекло для {}", taskId);
                emptyHolders.poll();
                return Session.DUMMY_SESSION();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException("task interrupted", e);
        } finally {
            responseLock.unlock();
        }
    }

    @Override
    public void putResult(final AsyncResponse response) {
        final RequestType type = response.getType();
        final ReentrantLock responseLock = responseLocks.get(type);
        responseLock.lock();
        try {
            if (type.isQueueResponse()) {
                final ResponseHolder holder = emptyHolderQueue.get(type).poll();
                if (holder != null) {
                    holder.setValue(response.getResult());
                    log.info("Получен ожидаемый асинхронный ответ с объектом типа {}", type);
                    fullHolderQueue.get(type).add(holder);
                    holder.getResponseCondition().signalAll();
                } else { //ответ пришел поздно
                    log.info("Получен опоздавший асинхронный ответ с объектом типа {}", type);
                }
            } else {

                final ResponseHolder holder = objectHolderMap.get(type);
                if (holder != null) {
                    holder.setValue(response.getResult());
                    log.info("Получен ожидаемый асинхронный ответ с объектом типа {}", type);
                    holder.getResponseCondition().signalAll();
                } else { //ответ пришел поздно
                    log.info("Получен опоздавший асинхронный ответ с объектом типа {}", type);
                }
            }
        } finally {
            responseLock.unlock();
        }
    }
}
