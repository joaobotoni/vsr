package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserResponse profile(UUID user) {
        User found = find(user);
        return show(found);
    }

    private User find(UUID user) {
        return userService.find(user);
    }

    private UserResponse show(User user) {
        return userMapper.response(user);
    }
}
