package ru.okoneva.concurrentlib.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.okoneva.concurrentlib.adapter.MyAdapter;
import ru.okoneva.concurrentlib.domain.Session;
import ru.okoneva.concurrentlib.domain.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping("${spring.application.name}")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Controller {

    private final MyAdapter adapter;

    @RequestMapping(value = "/getusers/", method = RequestMethod.GET)
    public List<User> getUsers() {
        final int[] ids = {1, 10, 2, 5, 8};
        return Arrays.stream(ids).parallel().mapToObj(adapter::findUser).collect(toList());
    }

    @RequestMapping(value = "/getsessions/", method = RequestMethod.GET)
    public List<Session> getSessions() {
        return IntStream.range(1, 9).parallel().mapToObj(adapter::generateSession).collect(toList());
    }
}
