package com.botoni.vsr.entity.person;

import com.botoni.vsr.converter.PersonTypeConverter;
import com.botoni.vsr.enums.PersonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "pessoas", name = "pessoa")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pessoa")
    private Integer id;

    @Convert(converter = PersonTypeConverter.class)
    @Column(name = "tipo", nullable = false, updatable = false)
    private PersonType type;

    @Column(name = "nome", nullable = false)
    private String name;

    protected Person(PersonType type, String name) {
        this.type = type;
        this.name = name.trim();
    }
}