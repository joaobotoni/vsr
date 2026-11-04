package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Membership;
import com.botoni.vsr.dto.response.MemberResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, Integer> {

    String ACTIVE = "from Membership m join fetch m.company c, User u "
            + "where u.uuid = :user and m.individual = u.person and m.removedAt is null";

    @Query("select m " + ACTIVE + " and c.uuid = :company")
    Optional<Membership> findActive(@Param("user") UUID user, @Param("company") UUID company);

    @Query("select m " + ACTIVE + " order by c.name")
    List<Membership> findAllActive(@Param("user") UUID user);

    @Query("select new com.botoni.vsr.dto.response.MemberResponse(u.uuid, i.name, u.email, m.role) "
            + "from Membership m join m.individual i, User u "
            + "where u.person = i and m.company.id = :company and m.removedAt is null order by i.name")
    List<MemberResponse> findMembers(@Param("company") Integer company);

    /** Vincula com o papel informado, ou reativa o vínculo desfeito com o papel novo. */
    @Modifying(flushAutomatically = true)
    @Query(value = "insert into vinculos.vinculo_pessoa_empresa (id_pessoa_fisica, id_pessoa_juridica, tipo) "
            + "values (:person, :company, cast(:role as vinculos.tipo_vinculo)) "
            + "on conflict (id_pessoa_fisica, id_pessoa_juridica) do update set tipo = excluded.tipo, removido_em = null",
            nativeQuery = true)
    int link(@Param("person") Integer person, @Param("company") Integer company, @Param("role") String role);

    @Modifying(flushAutomatically = true)
    @Query(value = "update vinculos.vinculo_pessoa_empresa set removido_em = now() "
            + "where id_pessoa_fisica = :person and id_pessoa_juridica = :company and removido_em is null", nativeQuery = true)
    int unlink(@Param("person") Integer person, @Param("company") Integer company);
}
