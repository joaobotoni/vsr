package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.UserRepository;
import com.botoni.vsr.exception.custom.UserException;
import com.botoni.vsr.exception.enums.problem.UserProblem;
import com.botoni.vsr.mapper.UserMapper;
import com.botoni.vsr.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public User save(Individual person, Email email) {
        User user = create(person, email);
        return persist(user);
    }

    @Transactional(readOnly = true)
    public User findWithPerson(UUID user) {
        return userRepository.findWithPersonByUuid(user)
                .orElseThrow(() -> new UserException(UserProblem.NOT_FOUND));
    }

    private User create(Individual person, Email email) {
        return userMapper.toEntity(person, email);
    }

    private User persist(User user) {
        return userRepository.save(user);
    }
}
