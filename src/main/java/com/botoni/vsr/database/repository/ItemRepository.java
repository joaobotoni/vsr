package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    @Query("select t from Item t join fetch t.room r join fetch r.inspection i where t.uuid = :item and t.removedAt is null "
            + "and r.removedAt is null and i.responsible.uuid = :user and i.removedAt is null")
    Optional<Item> findOwned(@Param("user") UUID user, @Param("item") UUID item);

    @Modifying(flushAutomatically = true)
    @Query(value = "update vistorias.item set removido_em = now() where id_item = :item", nativeQuery = true)
    int remove(@Param("item") Integer item);
}
