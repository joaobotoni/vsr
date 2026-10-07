package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Integer> {

    Optional<Session> findByIdAndDeviceUser(Integer id, User user);

    @Query("select s from Session s join fetch s.device d join fetch d.user where s.id = :session")
    Optional<Session> findWithUserById(@Param("session") Integer session);

    @Procedure(procedureName = "usuarios.registrar_acesso_sessao")
    void access(@Param("p_id_sessao") Integer session);

    @Procedure(procedureName = "usuarios.revogar_sessao")
    void revoke(@Param("p_id_sessao") Integer session);

    @Procedure(procedureName = "usuarios.revogar_sessoes_usuario")
    void revokeUser(@Param("p_id_usuario") Integer user, @Param("p_id_sessao_mantida") Integer keptSession);

    @Procedure(procedureName = "usuarios.revogar_sessoes_dispositivo")
    void revokeDevice(@Param("p_id_dispositivo") Integer device);
}