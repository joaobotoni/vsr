package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Item;
import com.botoni.vsr.database.entity.ItemType;
import com.botoni.vsr.database.entity.Room;
import com.botoni.vsr.dto.request.ItemEditRequest;
import com.botoni.vsr.dto.request.ItemRequest;
import com.botoni.vsr.dto.response.ItemResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfiguration.class)
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "room", source = "room")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "condition", source = "request.condition")
    @Mapping(target = "material", source = "request.material")
    @Mapping(target = "notes", source = "request.notes")
    Item entity(Room room, ItemType type, ItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "condition", ignore = true)
    @Mapping(target = "material", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "room", source = "room")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "name", source = "type.name")
    Item standard(Room room, ItemType type);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    void amend(ItemEditRequest request, @MappingTarget Item item);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "type", source = "type.uuid")
    @Mapping(target = "origin", source = "origin.uuid")
    ItemResponse response(Item item);
}
