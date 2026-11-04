package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.ItemType;
import com.botoni.vsr.database.entity.RoomType;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.ItemTypeRepository;
import com.botoni.vsr.database.repository.RoomTypeRepository;
import com.botoni.vsr.dto.request.ItemTypeRequest;
import com.botoni.vsr.dto.request.RoomTypeRequest;
import com.botoni.vsr.dto.response.ItemTypeResponse;
import com.botoni.vsr.dto.response.RoomTypeResponse;
import com.botoni.vsr.exception.custom.CatalogException;
import com.botoni.vsr.exception.enums.problem.CatalogProblem;
import com.botoni.vsr.mapper.CatalogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
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

    /** Teto das listas curtas, que não são paginadas. */
    private static final Limit LIST = Limit.of(200);

    private final RoomTypeRepository roomTypeRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final CatalogMapper catalogMapper;
    private final UserService userService;

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

    @Transactional
    public RoomTypeResponse addRoom(UUID user, RoomTypeRequest request) {
        User owner = owner(user);
        List<ItemType> items = chosen(user, request.items());
        RoomType type = saveRoom(owner, request);
        attach(type, items);
        return summary(type, items);
    }

    @Transactional
    public ItemTypeResponse addItem(UUID user, ItemTypeRequest request) {
        User owner = owner(user);
        ItemType type = saveItem(owner, request);
        return detail(type);
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
        return roomTypeRepository.findAllVisible(user, LIST);
    }

    private List<RoomTypeResponse> show(List<RoomType> types, UUID user) {
        return types.stream().map(type -> catalogMapper.room(type, contents(type, user))).toList();
    }

    private List<ItemType> available(UUID user) {
        return itemTypeRepository.findAllVisible(user, LIST);
    }

    private List<ItemTypeResponse> describe(List<ItemType> types) {
        return catalogMapper.items(types);
    }

    private User owner(UUID user) {
        return userService.find(user);
    }

    private List<ItemType> chosen(UUID user, List<UUID> items) {
        return items.stream().distinct().map(item -> existingItem(user, item)).toList();
    }

    private RoomType saveRoom(User owner, RoomTypeRequest request) {
        return roomTypeRepository.saveAndFlush(new RoomType(owner, request.name()));
    }

    private void attach(RoomType type, List<ItemType> items) {
        items.forEach(item -> roomTypeRepository.attach(type.getId(), item.getId()));
    }

    private RoomTypeResponse summary(RoomType type, List<ItemType> items) {
        return catalogMapper.room(type, items);
    }

    private ItemType saveItem(User owner, ItemTypeRequest request) {
        return itemTypeRepository.saveAndFlush(new ItemType(owner, request.name()));
    }

    private ItemTypeResponse detail(ItemType type) {
        return catalogMapper.item(type);
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
