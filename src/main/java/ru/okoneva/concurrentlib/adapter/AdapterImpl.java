package ru.okoneva.concurrentlib.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.okoneva.concurrentlib.domain.User;
import ru.okoneva.concurrentlib.service.MyService;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdapterImpl implements MyAdapter {

    private final MyService service;

    @Override
    public List<User> findUsers() {
        final Future<List<User>> future = service.findUser();

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
