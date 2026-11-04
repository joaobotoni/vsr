package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EvidenceRepository extends JpaRepository<Evidence, Integer> {

    @Query("select e from Evidence e join fetch e.room r join fetch r.inspection i where e.uuid = :evidence "
            + "and e.removedAt is null and r.removedAt is null and i.responsible.uuid = :user and i.removedAt is null")
    Optional<Evidence> findOwned(@Param("user") UUID user, @Param("evidence") UUID evidence);

    @Modifying(flushAutomatically = true)
    @Query(value = "update vistorias.evidencia set removido_em = now() where id_evidencia = :evidence", nativeQuery = true)
    int remove(@Param("evidence") Integer evidence);
}
