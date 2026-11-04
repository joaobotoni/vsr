package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Property;
import com.botoni.vsr.database.enums.PropertyCategory;
import com.botoni.vsr.database.enums.PropertyType;
import com.botoni.vsr.database.repository.PropertyRepository;
import com.botoni.vsr.dto.request.PropertyEditRequest;
import com.botoni.vsr.exception.custom.PropertyException;
import com.botoni.vsr.exception.enums.problem.PropertyProblem;
import com.botoni.vsr.mapper.PropertyMapper;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Imóveis")
class PropertyServiceTest {

    private static final UUID PROPERTY = UUID.fromString("9b2e4c1a-6d3f-4e8b-a7c5-1f0e2d3c4b5a");

    private final PropertyRepository propertyRepository = mock(PropertyRepository.class);
    private final PropertyService propertyService = new PropertyService(propertyRepository, mock(UserService.class),
            mock(AddressService.class), Mappers.getMapper(PropertyMapper.class));

    @Test
    @Controle
    @DisplayName("imóvel de outro usuário (ou removido) responde como inexistente")
    void foreignPropertyIsNotFound() {
        when(propertyRepository.findOwned(Users.UUID, PROPERTY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.view(Users.UUID, PROPERTY))
                .isInstanceOf(PropertyException.class)
                .extracting("problem").isEqualTo(PropertyProblem.NOT_FOUND);
    }

    @Test
    @Controle
    @DisplayName("a remoção de imóvel alheio é recusada antes de chegar ao banco")
    void foreignPropertyIsNotRemoved() {
        when(propertyRepository.findOwned(Users.UUID, PROPERTY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.remove(Users.UUID, PROPERTY)).isInstanceOf(PropertyException.class);

        verify(propertyRepository, never()).remove(any());
    }

    @Test
    @Controle
    @DisplayName("a remoção do próprio imóvel chama a remoção lógica pelo id interno")
    void ownPropertyIsRemovedLogically() {
        when(propertyRepository.findOwned(Users.UUID, PROPERTY)).thenReturn(Optional.of(property()));

        propertyService.remove(Users.UUID, PROPERTY);

        verify(propertyRepository).remove(7);
    }

    @Test
    @Controle
    @DisplayName("a edição muda só os campos enviados")
    void editKeepsMissingFields() {
        Property property = property();
        when(propertyRepository.findOwned(Users.UUID, PROPERTY)).thenReturn(Optional.of(property));

        propertyService.edit(Users.UUID, PROPERTY, new PropertyEditRequest(null, null, PropertyCategory.HIGH_END));

        assertThat(property.getDescription()).isEqualTo("Apartamento");
        assertThat(property.getType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(property.getCategory()).isEqualTo(PropertyCategory.HIGH_END);
    }

    private static Property property() {
        return Property.builder().id(7).uuid(PROPERTY).user(Users.ana()).description("Apartamento")
                .type(PropertyType.APARTMENT).category(PropertyCategory.RESIDENTIAL).build();
    }
}
