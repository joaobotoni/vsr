package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.ItemType;
import com.botoni.vsr.database.entity.RoomType;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.ItemTypeRepository;
import com.botoni.vsr.database.repository.RoomTypeRepository;
import com.botoni.vsr.exception.custom.CatalogException;
import com.botoni.vsr.exception.enums.problem.CatalogProblem;
import com.botoni.vsr.mapper.CatalogMapper;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Catálogo")
class CatalogServiceTest {

    private final RoomTypeRepository roomTypeRepository = mock(RoomTypeRepository.class);
    private final CatalogService catalogService = new CatalogService(roomTypeRepository, mock(ItemTypeRepository.class),
            Mappers.getMapper(CatalogMapper.class));

    @Test
    @Controle
    @DisplayName("os itens de um tipo de ambiente trazem só os padrão e os do próprio usuário")
    void contentsHideOtherUsersItems() {
        ItemType standard = item(true, null);
        ItemType mine = item(false, Users.ana());
        ItemType foreign = item(false, User.builder().id(2).uuid(UUID.randomUUID()).build());
        RoomType type = mock(RoomType.class);
        when(type.getItems()).thenReturn(List.of(standard, mine, foreign));

        assertThat(catalogService.contents(type, Users.UUID)).containsExactly(standard, mine);
    }

    @Test
    @Controle
    @DisplayName("tipo de ambiente que o usuário não vê responde como inexistente")
    void invisibleRoomTypeIsNotFound() {
        UUID type = UUID.randomUUID();
        when(roomTypeRepository.findVisible(Users.UUID, type)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.room(Users.UUID, type))
                .isInstanceOf(CatalogException.class)
                .extracting("problem").isEqualTo(CatalogProblem.ROOM_TYPE_NOT_FOUND);
    }

    private static ItemType item(boolean standard, User user) {
        ItemType item = mock(ItemType.class);
        when(item.isStandard()).thenReturn(standard);
        when(item.getUser()).thenReturn(user);
        return item;
    }
}
