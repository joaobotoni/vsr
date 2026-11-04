package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Inspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Toda consulta filtra pelo responsável e ignora as vistorias removidas.
 */
public interface InspectionRepository extends JpaRepository<Inspection, Integer> {

    String OWNED = "where i.responsible.uuid = :user and i.removedAt is null";

    String FILTERED = OWNED + " and (:property is null or p.uuid = :property) and (:status is null or cast(i.status as String) = :status)";

    @Query("select i from Inspection i join fetch i.property p " + OWNED + " and i.uuid = :inspection")
    Optional<Inspection> findOwned(@Param("user") UUID user, @Param("inspection") UUID inspection);

    @Query(value = "select i from Inspection i join fetch i.property p " + FILTERED,
            countQuery = "select count(i) from Inspection i join i.property p " + FILTERED)
    Page<Inspection> findAllOwned(@Param("user") UUID user,
                                  @Param("property") UUID property,
                                  @Param("status") String status,
                                  Pageable pageable);

    @Procedure(procedureName = "vistorias.copiar_vistoria")
    void copy(@Param("p_id_vistoria") Integer inspection);

    /** A data de finalização vem do relógio do banco, o mesmo que gravou o início. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update vistorias.vistoria set status = 'finalizada', finalizada_em = now() where id_vistoria = :inspection",
            nativeQuery = true)
    int finish(@Param("inspection") Integer inspection);

    @Modifying(flushAutomatically = true)
    @Query(value = "update vistorias.vistoria set removido_em = now() where id_vistoria = :inspection", nativeQuery = true)
    int remove(@Param("inspection") Integer inspection);
}
