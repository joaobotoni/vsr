package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.vo.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, Integer> {

    Optional<Device> findByUserAndIdentifier(User user, UUID identifier);

    boolean existsByUserEmailAndIdentifier(Email email, UUID identifier);

    @Procedure(procedureName = "usuarios.registrar_dispositivo")
    void upsert(@Param("p_id_usuario") Integer user,
                @Param("p_identificador") UUID identifier,
                @Param("p_plataforma") String platform,
                @Param("p_fabricante") String manufacturer,
                @Param("p_modelo") String model,
                @Param("p_versao_so") String osVersion);
}
