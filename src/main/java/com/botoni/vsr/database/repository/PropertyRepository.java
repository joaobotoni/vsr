package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Toda consulta filtra pelo dono e ignora os imóveis removidos: para quem pergunta, o imóvel de outro usuário não existe.
 */
public interface PropertyRepository extends JpaRepository<Property, Integer> {

    String WITH_ADDRESS = "select p from Property p join fetch p.address a join fetch a.street s "
            + "join fetch s.district d join fetch d.city c join fetch c.state ";

    String OWNED = "where p.user.uuid = :user and p.removedAt is null";

    @Query(WITH_ADDRESS + OWNED + " and p.uuid = :property")
    Optional<Property> findOwned(@Param("user") UUID user, @Param("property") UUID property);

    @Query(value = WITH_ADDRESS + OWNED, countQuery = "select count(p) from Property p " + OWNED)
    Page<Property> findAllOwned(@Param("user") UUID user, Pageable pageable);

    @Procedure(procedureName = "imoveis.remover_imovel")
    void remove(@Param("p_id_imovel") Integer property);
}
