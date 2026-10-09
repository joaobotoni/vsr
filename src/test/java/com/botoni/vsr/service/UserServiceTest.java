package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.UserRepository;
import com.botoni.vsr.exception.custom.UserException;
import com.botoni.vsr.exception.enums.problem.UserProblem;
import com.botoni.vsr.mapper.UserMapper;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Usuários")
class UserServiceTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final UserService userService = new UserService(repository, Mappers.getMapper(UserMapper.class));

    private final User user = User.builder().id(1).uuid(Users.UUID).email(Email.of(Users.EMAIL))
            .person(new Individual(Name.of("Ana"), Cpf.of("52998224725"))).build();

    @BeforeEach
    void storedUser() {
        when(repository.findWithPersonByUuid(any())).thenReturn(Optional.empty());
        when(repository.findWithPersonByUuid(Users.UUID)).thenReturn(Optional.of(user));
    }

    @Test
    @Controle
    @DisplayName("usuário é encontrado junto com a pessoa")
    void userIsFoundWithPerson() {
        assertThat(userService.find(Users.UUID).getPerson().getName()).isEqualTo(Name.of("Ana"));
    }

    @Test
    @Controle
    @DisplayName("busca com a pessoa por UUID desconhecido é recusada")
    void unknownUserWithPersonIsRejected() {
        assertThatThrownBy(() -> userService.find(UUID.randomUUID()))
                .isInstanceOf(UserException.class)
                .extracting("problem").isEqualTo(UserProblem.NOT_FOUND);
    }
}
