package com.botoni.vsr.entity.users;

import com.botoni.vsr.converter.DevicePlatformConverter;
import com.botoni.vsr.enums.DevicePlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.util.UUID;

@Entity
@Getter
@Setter
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
    @ColumnTransformer(write = "?::usuarios.plataforma_dispositivo")
    @Column(name = "plataforma", nullable = false)
    private DevicePlatform platform;

    @Column(name = "fabricante", nullable = false, length = 64)
    private String manufacturer;

    @Column(name = "modelo", nullable = false, length = 64)
    private String model;

    @Column(name = "versao_so", nullable = false, length = 16)
    private String osVersion;
}