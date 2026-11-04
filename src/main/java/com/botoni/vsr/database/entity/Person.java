package com.botoni.vsr.database.entity;

import com.botoni.vsr.database.enums.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Generated;

import java.util.UUID;

/**
 * Pessoa física ou jurídica. Sem {@code owner}, é a pessoa de uma conta; com {@code owner}, é um cadastro privado
 * desse usuário (proprietário, inquilino), invisível para os demais. O nome é validado pelo subtipo.
 */
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

    @Generated
    @Column(name = "uuid", nullable = false, insertable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_titular", updatable = false)
    private User owner;

    @ColumnTransformer(write = "?::pessoas.tipo_pessoa")
    @Column(name = "tipo", nullable = false, updatable = false)
    private PersonType type;

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "nome", nullable = false)
    private String name;

    /** Controle de concorrência: duas alterações simultâneas do mesmo registro, a segunda recebe 409. */
    @Version
    @Column(name = "versao", nullable = false)
    private Integer version;

    protected Person(User owner, PersonType type, String name) {
        this.owner = owner;
        this.type = type;
        this.name = name;
    }

    /** Troca o nome, validado pelas regras do subtipo; nulo mantém o atual. */
    public abstract void rename(String name);

    /** Troca o nome fantasia, que só a pessoa jurídica tem; nulo mantém o atual. */
    public void retitle(String tradeName) {
    }
}
