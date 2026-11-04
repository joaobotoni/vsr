package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.ItemType;
import com.botoni.vsr.database.entity.RoomType;
import com.botoni.vsr.database.repository.ItemTypeRepository;
import com.botoni.vsr.database.repository.RoomTypeRepository;
import com.botoni.vsr.dto.response.ItemTypeResponse;
import com.botoni.vsr.dto.response.RoomTypeResponse;
import com.botoni.vsr.exception.custom.CatalogException;
import com.botoni.vsr.exception.enums.problem.CatalogProblem;
import com.botoni.vsr.mapper.CatalogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Catálogo de tipos de ambiente e de item. O usuário vê os tipos padrão e os personalizados dele; o de outro usuário
 * responde 404, como se não existisse.
 */
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final RoomTypeRepository roomTypeRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final CatalogMapper catalogMapper;

    @Transactional(readOnly = true)
    public List<RoomTypeResponse> rooms(UUID user) {
        List<RoomType> types = visible(user);
        return show(types, user);
    }

    @Transactional(readOnly = true)
    public List<ItemTypeResponse> items(UUID user) {
        List<ItemType> types = available(user);
        return describe(types);
    }

    @Transactional(readOnly = true)
    public RoomType room(UUID user, UUID type) {
        return existingRoom(user, type);
    }

    @Transactional(readOnly = true)
    public ItemType item(UUID user, UUID type) {
        return existingItem(user, type);
    }

    @Transactional(readOnly = true)
    public List<ItemType> contents(RoomType type, UUID user) {
        return type.getItems().stream().filter(item -> accessible(item, user)).toList();
    }

    private List<RoomType> visible(UUID user) {
        return roomTypeRepository.findAllVisible(user);
    }

    private List<RoomTypeResponse> show(List<RoomType> types, UUID user) {
        return types.stream().map(type -> catalogMapper.room(type, contents(type, user))).toList();
    }

    private List<ItemType> available(UUID user) {
        return itemTypeRepository.findAllVisible(user);
    }

    private List<ItemTypeResponse> describe(List<ItemType> types) {
        return catalogMapper.items(types);
    }

    private RoomType existingRoom(UUID user, UUID type) {
        return roomTypeRepository.findVisible(user, type)
                .orElseThrow(() -> new CatalogException(CatalogProblem.ROOM_TYPE_NOT_FOUND));
    }

    private ItemType existingItem(UUID user, UUID type) {
        return itemTypeRepository.findVisible(user, type)
                .orElseThrow(() -> new CatalogException(CatalogProblem.ITEM_TYPE_NOT_FOUND));
    }

    private static boolean accessible(ItemType item, UUID user) {
        return item.isStandard() || item.getUser() != null && user.equals(item.getUser().getUuid());
    }
}
