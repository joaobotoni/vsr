package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Name;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface IndividualMapper {

    /** Pessoa de uma conta: sem titular. */
    default Individual entity(Name name, Cpf cpf) {
        return new Individual(null, name, cpf);
    }
}
