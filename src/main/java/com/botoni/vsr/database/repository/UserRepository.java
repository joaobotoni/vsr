package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.vo.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u from User u join fetch u.person where u.uuid = :uuid")
    Optional<User> findWithPersonByUuid(@Param("uuid") UUID uuid);

    @Query("select u from User u join fetch u.person where u.email = :email")
    Optional<User> findWithPersonByEmail(@Param("email") Email email);
}
