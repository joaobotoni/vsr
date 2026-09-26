package com.botoni.vsr.entity;

import com.botoni.vsr.converter.PersonTypeConverter;
import com.botoni.vsr.enums.PersonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

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
    @ColumnTransformer(write = "?::pessoas.tipo_pessoa")
    private PersonType type;

    @Column(name = "nome", nullable = false)
    private String name;

    protected Person(PersonType type, String name) {
        this.type = type;
        this.name = name.trim();
    }
}