package com.botoni.vsr.entity;

import com.botoni.vsr.enums.PersonType;
import com.botoni.vsr.vo.Cpf;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "pessoas", name = "pessoa_fisica")
@PrimaryKeyJoinColumn(name = "id_pessoa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Individual extends Person {

    @Column(name = "cpf", nullable = false, updatable = false)
    private String cpf;

    public Individual(String name, Cpf cpf) {
        super(PersonType.INDIVIDUAL, name);
        this.cpf = cpf.value();
    }
}
