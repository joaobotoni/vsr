package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Item;
import com.botoni.vsr.database.entity.ItemType;
import com.botoni.vsr.database.entity.Room;
import com.botoni.vsr.database.repository.ItemRepository;
import com.botoni.vsr.dto.request.ItemEditRequest;
import com.botoni.vsr.dto.request.ItemRequest;
import com.botoni.vsr.dto.response.ItemResponse;
import com.botoni.vsr.exception.custom.InspectionException;
import com.botoni.vsr.exception.enums.problem.InspectionProblem;
import com.botoni.vsr.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Itens dos ambientes das vistorias do usuário.
 */
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final RoomService roomService;
    private final CatalogService catalogService;
    private final ItemMapper itemMapper;

    @Transactional
    public ItemResponse add(UUID user, UUID room, ItemRequest request) {
        Room found = room(user, room);
        ItemType type = type(user, request.type());
        Item item = save(found, type, request);
        return show(item);
    }

    @Transactional
    public ItemResponse edit(UUID user, UUID item, ItemEditRequest request) {
        Item found = owned(user, item);
        amend(found, request);
        store(found);
        return show(found);
    }

    @Transactional
    public void remove(UUID user, UUID item) {
        Item found = owned(user, item);
        discard(found);
    }

    private Room room(UUID user, UUID room) {
        return roomService.find(user, room);
    }

    private Item save(Room room, ItemType type, ItemRequest request) {
        return itemRepository.saveAndFlush(create(room, type, request));
    }

    private Item create(Room room, ItemType type, ItemRequest request) {
        return itemMapper.entity(room, type, request);
    }

    private ItemResponse show(Item item) {
        return itemMapper.response(item);
    }

    private void amend(Item item, ItemEditRequest request) {
        itemMapper.amend(request, item);
    }

    private void store(Item item) {
        itemRepository.saveAndFlush(item);
    }

    private void discard(Item item) {
        itemRepository.remove(item.getId());
    }

    private ItemType type(UUID user, UUID type) {
        if (type == null) {
            return null;
        }
        return catalogService.item(user, type);
    }

    private Item owned(UUID user, UUID item) {
        return itemRepository.findOwned(user, item)
                .orElseThrow(() -> new InspectionException(InspectionProblem.ITEM_NOT_FOUND));
    }
}
