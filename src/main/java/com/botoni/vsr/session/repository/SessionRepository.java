package com.botoni.vsr.session.repository;

import com.botoni.vsr.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {
}
