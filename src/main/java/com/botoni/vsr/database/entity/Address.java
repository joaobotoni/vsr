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

/**
 * Endereço somente leitura: a cadeia estado, cidade, bairro, logradouro e endereço é gravada pela função
 * {@code enderecos.registrar_endereco}, que reaproveita o que já existe.
 */
@Entity
@Getter
@Immutable
@Table(schema = "enderecos", name = "endereco")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id
    @Column(name = "id_endereco")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_logradouro", nullable = false)
    private Street street;

    @Column(name = "numero", nullable = false)
    private String number;

    @Column(name = "complemento")
    private String complement;
}
