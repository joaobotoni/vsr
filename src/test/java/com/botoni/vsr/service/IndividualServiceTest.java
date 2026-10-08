package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.enums.PersonType;
import com.botoni.vsr.database.repository.IndividualRepository;
import com.botoni.vsr.mapper.IndividualMapper;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Pessoa física")
class IndividualServiceTest {

    private final IndividualRepository repository = mock(IndividualRepository.class);
    private final IndividualService individualService =
            new IndividualService(repository, Mappers.getMapper(IndividualMapper.class));

    @Test
    @Controle
    @DisplayName("salvar grava uma pessoa física com o nome e o CPF informados")
    void saveStoresIndividual() {
        when(repository.save(any())).then(returnsFirstArg());

        Individual saved = individualService.save(Name.of("Ana Maria"), Cpf.of("529.982.247-25"));

        assertThat(saved.getType()).isEqualTo(PersonType.INDIVIDUAL);
        assertThat(saved.getName()).isEqualTo(Name.of("Ana Maria"));
        assertThat(saved.getCpf()).isEqualTo(Cpf.of("52998224725"));
        verify(repository).save(saved);
    }
}
