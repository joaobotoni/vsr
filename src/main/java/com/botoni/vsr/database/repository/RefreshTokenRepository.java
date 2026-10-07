package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByCurrentHash(byte[] currentHash);

    @Query(value = "select r.* from usuarios.refresh_token r join usuarios.refresh_token_usado u on u.id_sessao = r.id_sessao where u.hash = :hash", nativeQuery = true)
    Optional<RefreshToken> findByUsedHash(@Param("hash") byte[] hash);

    @Procedure(procedureName = "usuarios.emitir_refresh_token")
    void issue(@Param("p_id_sessao") Integer session, @Param("p_hash") byte[] hash);

    @Procedure(procedureName = "usuarios.renovar_refresh_token")
    void renew(@Param("p_id_sessao") Integer session, @Param("p_hash_atual") byte[] currentHash, @Param("p_hash_novo") byte[] newHash);
}
