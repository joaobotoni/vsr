package com.botoni.vsr.database.entity;

import com.botoni.vsr.vo.Cep;
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
@Table(schema = "enderecos", name = "logradouro")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Street {

    @Id
    @Column(name = "id_logradouro")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bairro", nullable = false)
    private District district;

    @Column(name = "cep", nullable = false)
    private Cep cep;

    @Column(name = "nome", nullable = false)
    private String name;
}
