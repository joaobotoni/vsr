package com.botoni.vsr.entity;

import com.botoni.vsr.enums.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "pessoas", name = "pessoa_juridica")
@PrimaryKeyJoinColumn(name = "id_pessoa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends Person {

    @Column(name = "cnpj", nullable = false, updatable = false)
    private String cnpj;

    @Column(name = "nome_fantasia", nullable = false)
    private String tradeName;

    public Company(String legalName, String tradeName, String cnpj) {
        super(PersonType.COMPANY, legalName);
        this.tradeName = tradeName;
        this.cnpj = cnpj;
    }
}
