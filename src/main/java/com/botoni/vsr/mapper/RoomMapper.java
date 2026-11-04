package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Inspection;
import com.botoni.vsr.database.entity.Room;
import com.botoni.vsr.database.entity.RoomType;
import com.botoni.vsr.dto.request.RoomEditRequest;
import com.botoni.vsr.dto.request.RoomRequest;
import com.botoni.vsr.dto.response.RoomResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfiguration.class, uses = {ItemMapper.class, EvidenceMapper.class})
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "evidences", ignore = true)
    @Mapping(target = "inspection", source = "inspection")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "notes", source = "request.notes")
    Room entity(Inspection inspection, RoomType type, RoomRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "inspection", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "evidences", ignore = true)
    void amend(RoomEditRequest request, @MappingTarget Room room);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "type", source = "type.uuid")
    @Mapping(target = "origin", source = "origin.uuid")
    @Mapping(target = "evidences", source = "evidences", defaultExpression = "java(java.util.List.of())")
    RoomResponse response(Room room);
}
