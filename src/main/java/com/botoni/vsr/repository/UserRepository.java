package com.botoni.vsr.repository;

import com.botoni.vsr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("select u from User u join fetch u.person where u.id = :id")
    Optional<User> findWithPersonById(@Param("id") Integer id);
}
