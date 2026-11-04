package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * O usuário vê os tipos padrão e os personalizados dele.
 */
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {

    String VISIBLE = "select t from RoomType t left join t.user u where (t.standard = true or u.uuid = :user)";

    @Query(VISIBLE + " order by t.name")
    List<RoomType> findAllVisible(@Param("user") UUID user);

    @Query(VISIBLE + " and t.uuid = :type")
    Optional<RoomType> findVisible(@Param("user") UUID user, @Param("type") UUID type);
}
