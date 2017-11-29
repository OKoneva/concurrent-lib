package ru.okoneva.concurrentlib.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.okoneva.concurrentlib.domain.User;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class MyServiceImpl implements MyService{

    @Value("${concurrentlib.thread.number}")
    private final int nThreads = 1;

    private ExecutorService executor = Executors.newFixedThreadPool(nThreads);

    public Future<List<User>> findUser() {
        System.out.println(nThreads);
        Callable<List<User>> task = () -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                return Arrays.asList(new User("Peter"), new User("Mark"));
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };
        Future<List<User>> future = executor.submit(task);
        return future;

    }
}
