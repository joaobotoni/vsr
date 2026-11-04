package com.botoni.vsr.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

/**
 * Tipo de item do catálogo: padrão do sistema ({@code padrao}) ou personalizado por um usuário. Criado, não muda.
 */
@Entity
@Getter
@Immutable
@BatchSize(size = 50)
@Table(schema = "catalogo", name = "tipo_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_item")
    private Integer id;

    @Generated
    @Column(name = "uuid", nullable = false, insertable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private User user;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "padrao", nullable = false)
    private boolean standard;

    /** Tipo personalizado do usuário. */
    public ItemType(User user, String name) {
        this.user = user;
        this.name = name;
    }
}
