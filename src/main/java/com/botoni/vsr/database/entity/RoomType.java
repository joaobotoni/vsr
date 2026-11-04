package com.botoni.vsr.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Immutable;

import java.util.List;
import java.util.UUID;

/**
 * Tipo de ambiente do catálogo, com os itens típicos dele. Criado, não muda.
 */
@Entity
@Getter
@Immutable
@BatchSize(size = 50)
@Table(schema = "catalogo", name = "tipo_ambiente")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_ambiente")
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

    @BatchSize(size = 50)
    @ManyToMany(fetch = FetchType.LAZY)
    @OrderBy("name")
    @JoinTable(schema = "catalogo", name = "tipo_ambiente_item",
            joinColumns = @JoinColumn(name = "id_tipo_ambiente"),
            inverseJoinColumns = @JoinColumn(name = "id_tipo_item"))
    private List<ItemType> items;

    /** Tipo personalizado do usuário; os itens são ligados depois, no repositório. */
    public RoomType(User user, String name) {
        this.user = user;
        this.name = name;
        this.items = List.of();
    }
}
