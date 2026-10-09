package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.repository.IndividualRepository;
import com.botoni.vsr.mapper.IndividualMapper;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Name;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndividualService {

    private final IndividualRepository individualRepository;
    private final IndividualMapper individualMapper;

    @Transactional
    public Individual save(Name name, Cpf cpf) {
        return individualRepository.save(create(name, cpf));
    }

    private Individual create(Name name, Cpf cpf) {
        return individualMapper.entity(name, cpf);
    }
}