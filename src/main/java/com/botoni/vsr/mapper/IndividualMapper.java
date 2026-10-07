package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.vo.Cpf;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface IndividualMapper {
    Individual toEntity(String name, Cpf cpf);
}
