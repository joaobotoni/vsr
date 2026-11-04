package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    @Query("select r from Room r join fetch r.inspection i where r.uuid = :room and r.removedAt is null "
            + "and " + Access.INSPECTION + "and i.removedAt is null")
    Optional<Room> findOwned(@Param("user") UUID user, @Param("room") UUID room);

    @Modifying(flushAutomatically = true)
    @Query(value = "update vistorias.ambiente set removido_em = now() where id_ambiente = :room", nativeQuery = true)
    int remove(@Param("room") Integer room);
}
