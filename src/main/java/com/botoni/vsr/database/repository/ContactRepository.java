package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
}
