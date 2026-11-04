package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Ownership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OwnershipRepository extends JpaRepository<Ownership, Ownership.Key> {

    @Query("select o from Ownership o join fetch o.person p where o.property.id = :property and o.removedAt is null order by p.name")
    List<Ownership> findActive(@Param("property") Integer property);

    /** Vincula, ou reativa o vínculo desfeito. */
    @Modifying(flushAutomatically = true)
    @Query(value = "insert into imoveis.imovel_proprietario (id_imovel, id_pessoa) values (:property, :person) "
            + "on conflict (id_imovel, id_pessoa) do update set removido_em = null", nativeQuery = true)
    int link(@Param("property") Integer property, @Param("person") Integer person);

    @Modifying(flushAutomatically = true)
    @Query(value = "update imoveis.imovel_proprietario set removido_em = now() "
            + "where id_imovel = :property and id_pessoa = :person and removido_em is null", nativeQuery = true)
    int unlink(@Param("property") Integer property, @Param("person") Integer person);
}
