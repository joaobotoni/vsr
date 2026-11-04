package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.ItemType;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * O usuário vê os tipos padrão e os personalizados dele.
 */
public interface ItemTypeRepository extends JpaRepository<ItemType, Integer> {

    String VISIBLE = "select t from ItemType t left join t.user u where (t.standard = true or u.uuid = :user)";

    @Query(VISIBLE + " order by t.name")
    List<ItemType> findAllVisible(@Param("user") UUID user, Limit limit);

    @Query(VISIBLE + " and t.uuid = :type")
    Optional<ItemType> findVisible(@Param("user") UUID user, @Param("type") UUID type);
}
