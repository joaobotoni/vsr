package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Inspection;
import com.botoni.vsr.database.entity.Property;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.InspectionEditRequest;
import com.botoni.vsr.dto.request.InspectionRequest;
import com.botoni.vsr.dto.response.InspectionDetailResponse;
import com.botoni.vsr.dto.response.InspectionResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfiguration.class, uses = RoomMapper.class)
public interface InspectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "status", constant = "IN_PROGRESS")
    @Mapping(target = "responsible", source = "user")
    @Mapping(target = "property", source = "property")
    @Mapping(target = "entry", source = "entry")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "notes", source = "request.notes")
    Inspection entity(User user, Property property, Inspection entry, InspectionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "entry", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "company", ignore = true)
    void amend(InspectionEditRequest request, @MappingTarget Inspection inspection);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "property", source = "property.uuid")
    @Mapping(target = "entry", source = "entry.uuid")
    InspectionResponse response(Inspection inspection);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "property", source = "property.uuid")
    @Mapping(target = "entry", source = "entry.uuid")
    InspectionDetailResponse detail(Inspection inspection);
}
