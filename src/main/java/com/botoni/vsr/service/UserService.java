package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.UserRepository;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.exception.custom.UserException;
import com.botoni.vsr.exception.enums.problem.UserProblem;
import com.botoni.vsr.mapper.RegisterMapper;
import com.botoni.vsr.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RegisterMapper registerMapper;

    @Transactional
    public User save(RegisterRequest request) {
        User user = create(request);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse find(Integer id) {
        User user = findWithPerson(id);
        return respond(user);
    }

    private User create(RegisterRequest request) {
        return registerMapper.toEntity(request);
    }

    private User findWithPerson(Integer id) {
        return userRepository.findWithPersonById(id)
                .orElseThrow(() -> new UserException(UserProblem.NOT_FOUND));
    }

    private UserResponse respond(User user) {
        return userMapper.toResponse(user);
    }
}
