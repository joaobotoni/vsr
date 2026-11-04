package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Só o cadastro privado do usuário: a pessoa de outro usuário, ou de uma conta, não existe para quem pergunta.
 */
public interface PersonRepository extends JpaRepository<Person, Integer> {

    @Query("select p from Person p where p.owner.uuid = :user and p.uuid = :person")
    Optional<Person> findOwned(@Param("user") UUID user, @Param("person") UUID person);

    @Query(value = "select p from Person p where p.owner.uuid = :user",
            countQuery = "select count(p) from Person p where p.owner.uuid = :user")
    Page<Person> findAllOwned(@Param("user") UUID user, Pageable pageable);
}
