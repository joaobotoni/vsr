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

/**
 * Pessoa jurídica: o nome é a razão social (texto livre, já normalizado na entrada), com o nome fantasia à parte.
 */
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

    public Company(User owner, String legalName, String tradeName, Cnpj cnpj) {
        super(owner, PersonType.COMPANY, legalName);
        this.tradeName = tradeName;
        this.cnpj = cnpj;
    }

    @Override
    public void rename(String legalName) {
        if (legalName == null) {
            return;
        }
        setName(legalName);
    }

    @Override
    public void retitle(String tradeName) {
        if (tradeName == null) {
            return;
        }
        this.tradeName = tradeName;
    }
}
