package ru.okoneva.concurrentlib.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.okoneva.concurrentlib.adapter.MyAdapter;
import ru.okoneva.concurrentlib.domain.User;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("${spring.application.name}")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Controller {

    private final MyAdapter adapter;

    @RequestMapping(value = "/getusers/", method = RequestMethod.GET)
    public List<User> getUsers() {
        return adapter.findUsers();
    }
}
