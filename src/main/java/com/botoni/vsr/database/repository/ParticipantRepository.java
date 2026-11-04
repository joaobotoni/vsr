package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Participant.Key> {

    @Query("select a from Participant a join fetch a.person p where a.inspection.id = :inspection and a.removedAt is null order by p.name")
    List<Participant> findActive(@Param("inspection") Integer inspection);

    /** Vincula com o papel informado, ou reativa o vínculo desfeito com o papel novo. */
    @Modifying(flushAutomatically = true)
    @Query(value = "insert into vistorias.vistoria_pessoa (id_vistoria, id_pessoa, tipo_vinculo) "
            + "values (:inspection, :person, cast(:role as vistorias.tipo_vinculo_pessoa)) "
            + "on conflict (id_vistoria, id_pessoa) do update set tipo_vinculo = excluded.tipo_vinculo, removido_em = null",
            nativeQuery = true)
    int link(@Param("inspection") Integer inspection, @Param("person") Integer person, @Param("role") String role);

    @Modifying(flushAutomatically = true)
    @Query(value = "update vistorias.vistoria_pessoa set removido_em = now() "
            + "where id_vistoria = :inspection and id_pessoa = :person and removido_em is null", nativeQuery = true)
    int unlink(@Param("inspection") Integer inspection, @Param("person") Integer person);
}
