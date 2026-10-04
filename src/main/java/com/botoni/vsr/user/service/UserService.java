package com.botoni.vsr.user.service;

import com.botoni.vsr.user.dto.response.UserResponse;
import com.botoni.vsr.user.exception.UserNotFoundException;
import com.botoni.vsr.user.mapper.UserMapper;
import com.botoni.vsr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse findById(Integer id) {
        return userRepository.findWithPersonById(id)
                .map(userMapper::toResponse)
                .orElseThrow(UserNotFoundException::new);
    }
}
