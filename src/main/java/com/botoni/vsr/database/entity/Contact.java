package com.botoni.vsr.database.entity;

import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Phone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Contato da pessoa: telefone, e-mail ou os dois (o banco exige ao menos um).
 */
@Entity
@Getter
@Table(schema = "pessoas", name = "pessoa_contato")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contact {

    @Id
    @Column(name = "id_pessoa")
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pessoa")
    private Person person;

    @Column(name = "telefone")
    private Phone phone;

    @Column(name = "email")
    private Email email;

    public Contact(Person person) {
        this.person = person;
    }

    /** Troca só o que veio preenchido; o resto fica como está. */
    public Contact update(Phone phone, Email email) {
        this.phone = phone == null ? this.phone : phone;
        this.email = email == null ? this.email : email;
        return this;
    }
}
