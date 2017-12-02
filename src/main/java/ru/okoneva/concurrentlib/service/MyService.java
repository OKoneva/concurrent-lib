package ru.okoneva.concurrentlib.service;

import ru.okoneva.concurrentlib.domain.SyncResponse;

public interface MyService {

    SyncResponse findUser(int id);

    SyncResponse generateSession();


}
