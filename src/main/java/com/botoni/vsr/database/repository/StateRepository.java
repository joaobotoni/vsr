package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StateRepository extends JpaRepository<State, Integer> {

    List<State> findAllByOrderByAcronymAsc();
}
