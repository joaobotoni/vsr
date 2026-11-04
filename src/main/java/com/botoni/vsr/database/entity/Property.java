package com.botoni.vsr.database.entity;

import com.botoni.vsr.database.enums.PropertyCategory;
import com.botoni.vsr.database.enums.PropertyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Generated;

import java.time.Instant;
import java.util.UUID;

/**
 * Imóvel. A remoção é lógica ({@code removido_em}) e feita só pela procedure {@code imoveis.remover_imovel}.
 */
@Entity
@Getter
@Setter
@Builder
@Table(schema = "imoveis", name = "imovel")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imovel")
    private Integer id;

    @Generated
    @Column(name = "uuid", nullable = false, insertable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_endereco", nullable = false, updatable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pessoa_juridica", updatable = false)
    private Company company;

    @Column(name = "descricao")
    private String description;

    @ColumnTransformer(write = "?::imoveis.tipo_imovel")
    @Column(name = "tipo", nullable = false)
    private PropertyType type;

    @ColumnTransformer(write = "?::imoveis.categoria_imovel")
    @Column(name = "categoria", nullable = false)
    private PropertyCategory category;

    @Column(name = "removido_em", insertable = false, updatable = false)
    private Instant removedAt;

    /** Controle de concorrência: duas alterações simultâneas do mesmo registro, a segunda recebe 409. */
    @Version
    @Column(name = "versao", nullable = false)
    private Integer version;
}
