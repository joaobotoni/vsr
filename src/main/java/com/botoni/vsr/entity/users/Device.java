package com.botoni.vsr.entity.users;

import com.botoni.vsr.converter.DevicePlatformConverter;
import com.botoni.vsr.enums.DevicePlatform;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Builder
@Table(schema = "usuarios", name = "dispositivo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dispositivo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, updatable = false)
    private User user;

    @Column(name = "identificador", nullable = false, updatable = false)
    private UUID identifier;

    @Convert(converter = DevicePlatformConverter.class)
    @Column(name = "plataforma", nullable = false)
    private DevicePlatform platform;

    @Column(name = "fabricante")
    private String manufacturer;

    @Column(name = "modelo")
    private String model;

    @Column(name = "versao_so")
    private String osVersion;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ultimo_ip", nullable = false)
    private InetAddress lastIp;

    @Builder.Default
    @Column(name = "ultimo_acesso_em", nullable = false)
    private Instant lastAccessAt = Instant.now();

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}