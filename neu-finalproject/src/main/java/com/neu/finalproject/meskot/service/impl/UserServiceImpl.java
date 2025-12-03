package com.neu.finalproject.meskot.service.impl;

import com.neu.finalproject.meskot.model.User;

import java.util.Optional;

public interface UserServiceImpl {
    User register(User user);
    Optional<User> login(String username, String password);
    Optional<User> findByUsername(String username);
}
