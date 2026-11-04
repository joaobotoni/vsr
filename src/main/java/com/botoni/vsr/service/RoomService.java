package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Inspection;
import com.botoni.vsr.database.entity.Item;
import com.botoni.vsr.database.entity.Room;
import com.botoni.vsr.database.entity.RoomType;
import com.botoni.vsr.database.repository.ItemRepository;
import com.botoni.vsr.database.repository.RoomRepository;
import com.botoni.vsr.dto.request.RoomEditRequest;
import com.botoni.vsr.dto.request.RoomRequest;
import com.botoni.vsr.dto.response.RoomResponse;
import com.botoni.vsr.exception.custom.InspectionException;
import com.botoni.vsr.exception.enums.problem.InspectionProblem;
import com.botoni.vsr.mapper.ItemMapper;
import com.botoni.vsr.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Ambientes das vistorias do usuário. Com tipo do catálogo, o ambiente nasce com os itens típicos dele.
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ItemRepository itemRepository;
    private final InspectionService inspectionService;
    private final CatalogService catalogService;
    private final RoomMapper roomMapper;
    private final ItemMapper itemMapper;

    @Transactional
    public RoomResponse add(UUID user, UUID inspection, RoomRequest request) {
        Inspection found = inspection(user, inspection);
        RoomType type = type(user, request.type());
        Room room = save(found, type, request);
        List<Item> items = items(room, type, user);
        attach(room, items);
        return show(room);
    }

    @Transactional
    public RoomResponse edit(UUID user, UUID room, RoomEditRequest request) {
        Room found = owned(user, room);
        amend(found, request);
        store(found);
        return show(found);
    }

    @Transactional
    public void remove(UUID user, UUID room) {
        Room found = owned(user, room);
        discard(found);
    }

    @Transactional(readOnly = true)
    public Room find(UUID user, UUID room) {
        return owned(user, room);
    }

    private Inspection inspection(UUID user, UUID inspection) {
        return inspectionService.find(user, inspection);
    }

    private Room save(Inspection inspection, RoomType type, RoomRequest request) {
        return roomRepository.saveAndFlush(create(inspection, type, request));
    }

    private Room create(Inspection inspection, RoomType type, RoomRequest request) {
        return roomMapper.entity(inspection, type, request);
    }

    private List<Item> standard(Room room, RoomType type, UUID user) {
        return itemRepository.saveAllAndFlush(catalogService.contents(type, user).stream()
                .map(item -> itemMapper.standard(room, item)).toList());
    }

    private void attach(Room room, List<Item> items) {
        room.setItems(items);
    }

    private RoomResponse show(Room room) {
        return roomMapper.response(room);
    }

    private void amend(Room room, RoomEditRequest request) {
        roomMapper.amend(request, room);
    }

    private void store(Room room) {
        roomRepository.saveAndFlush(room);
    }

    private void discard(Room room) {
        roomRepository.remove(room.getId());
    }

    private RoomType type(UUID user, UUID type) {
        if (type == null) {
            return null;
        }
        return catalogService.room(user, type);
    }

    private List<Item> items(Room room, RoomType type, UUID user) {
        if (type == null) {
            return List.of();
        }
        return standard(room, type, user);
    }

    private Room owned(UUID user, UUID room) {
        return roomRepository.findOwned(user, room)
                .orElseThrow(() -> new InspectionException(InspectionProblem.ROOM_NOT_FOUND));
    }
}
