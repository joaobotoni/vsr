package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Evidence;
import com.botoni.vsr.database.entity.Item;
import com.botoni.vsr.database.entity.Room;
import com.botoni.vsr.dto.response.EvidenceResponse;
import com.botoni.vsr.vo.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapperConfiguration.class)
public interface EvidenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "room", source = "room")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "path", source = "path")
    @Mapping(target = "contentType", source = "photo.contentType")
    @Mapping(target = "size", expression = "java(photo.size())")
    @Mapping(target = "hash", expression = "java(photo.hash())")
    @Mapping(target = "receivedAt", source = "receivedAt")
    @Mapping(target = "description", source = "description")
    Evidence entity(Room room, Item item, String path, Photo photo, Instant receivedAt, String description);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "item", source = "item.uuid")
    EvidenceResponse response(Evidence evidence);
}
