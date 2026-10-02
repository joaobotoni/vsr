package com.botoni.vsr.repository;

import com.botoni.vsr.entity.users.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {
}
