package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.vo.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LocalCredentialRepository extends JpaRepository<LocalCredential, Integer> {

    Optional<LocalCredential> findByUserUuid(UUID uuid);

    @Query("select c from LocalCredential c join fetch c.user u where u.email = :email")
    Optional<LocalCredential> findWithUserByEmail(@Param("email") Email email);

    @Procedure(procedureName = "usuarios.trocar_senha")
    void change(@Param("p_id_usuario") Integer user,
                @Param("p_hash_atual") String currentHash,
                @Param("p_hash_novo") String newHash);
}
