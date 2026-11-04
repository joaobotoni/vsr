package com.botoni.vsr.database.entity;

import com.botoni.vsr.database.enums.PersonType;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Name;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pessoa física: o nome segue as regras de {@link Name}.
 */
@Entity
@Getter
@Table(schema = "pessoas", name = "pessoa_fisica")
@PrimaryKeyJoinColumn(name = "id_pessoa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Individual extends Person {

    @Column(name = "cpf", nullable = false, updatable = false)
    private Cpf cpf;

    public Individual(User owner, Name name, Cpf cpf) {
        super(owner, PersonType.INDIVIDUAL, name.value());
        this.cpf = cpf;
    }

    @Override
    public void rename(String name) {
        if (name == null) {
            return;
        }
        setName(Name.of(name).value());
    }
}
