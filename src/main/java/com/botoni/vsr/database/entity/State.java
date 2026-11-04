package com.botoni.vsr.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@Table(schema = "enderecos", name = "estado")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class State {

    @Id
    @Column(name = "id_estado")
    private Integer id;

    @Column(name = "sigla", nullable = false)
    private String acronym;

    @Column(name = "nome", nullable = false)
    private String name;
}
