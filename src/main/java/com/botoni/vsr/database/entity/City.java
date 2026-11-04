package com.botoni.vsr.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@Table(schema = "enderecos", name = "cidade")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class City {

    @Id
    @Column(name = "id_cidade")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private State state;

    @Column(name = "nome", nullable = false)
    private String name;
}
