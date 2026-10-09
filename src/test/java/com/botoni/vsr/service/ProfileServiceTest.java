package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.response.UserResponse;
import com.botoni.vsr.exception.custom.UserException;
import com.botoni.vsr.exception.enums.problem.UserProblem;
import com.botoni.vsr.mapper.UserMapper;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Perfil")
class ProfileServiceTest {

    private final UserService userService = mock(UserService.class);
    private final ProfileService profileService = new ProfileService(userService, Mappers.getMapper(UserMapper.class));

    @Test
    @Controle
    @DisplayName("o perfil expõe o UUID público, não o id do banco")
    void profileExposesUuid() {
        User user = User.builder().id(1).uuid(Users.UUID).email(Email.of(Users.EMAIL))
                .person(new Individual(Name.of("Ana"), Cpf.of("52998224725"))).build();
        when(userService.find(Users.UUID)).thenReturn(user);

        UserResponse response = profileService.profile(Users.UUID);

        assertThat(response.id()).isEqualTo(Users.UUID);
    }

    @Test
    @Controle
    @DisplayName("perfil de usuário inexistente é recusado")
    void unknownProfileIsRejected() {
        UUID unknown = UUID.randomUUID();
        when(userService.find(unknown)).thenThrow(new UserException(UserProblem.NOT_FOUND));

        assertThatThrownBy(() -> profileService.profile(unknown))
                .isInstanceOf(UserException.class)
                .extracting("problem").isEqualTo(UserProblem.NOT_FOUND);
    }
}
