package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Individual;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndividualRepository extends JpaRepository<Individual, Integer> {
}
