package com.botoni.vsr.entity.person;

import com.botoni.vsr.converter.CpfConverter;
import com.botoni.vsr.enums.PersonType;
import com.botoni.vsr.vo.Cpf;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "pessoas", name = "pessoa_fisica")
@PrimaryKeyJoinColumn(name = "id_pessoa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Individual extends Person {

    @Convert(converter = CpfConverter.class)
    @Column(name = "cpf", nullable = false, updatable = false)
    private Cpf cpf;

    public Individual(String name, Cpf cpf) {
        super(PersonType.INDIVIDUAL, name);
        this.cpf = cpf;
    }
}