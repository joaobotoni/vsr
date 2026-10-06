package com.botoni.vsr.database.entity;

import com.botoni.vsr.database.enums.PersonType;
import com.botoni.vsr.vo.Cnpj;
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
    private Cnpj cnpj;

    @Column(name = "nome_fantasia", nullable = false)
    private String tradeName;

    public Company(String legalName, String tradeName, Cnpj cnpj) {
        super(PersonType.COMPANY, legalName);
        this.tradeName = tradeName.trim();
        this.cnpj = cnpj;
    }
}
