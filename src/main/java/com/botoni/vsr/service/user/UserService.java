package com.botoni.vsr.service.user;

import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.exception.infrastructure.NotFoundException;
import com.botoni.vsr.mapper.user.UserMapper;
import com.botoni.vsr.repository.UserRepository;
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
                .orElseThrow(NotFoundException.Usuario::new);
    }
}
