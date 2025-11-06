package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.User;
import com.neu.finalproject.meskot.repository.UserRepository;
import com.neu.finalproject.meskot.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService  implements UserServiceImpl {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User register(User user) {
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> login(String username, String password) {
        Optional<User> user = findByUsername(username);
        if (user.isPresent() && BCrypt.checkpw(password, user.get().getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
