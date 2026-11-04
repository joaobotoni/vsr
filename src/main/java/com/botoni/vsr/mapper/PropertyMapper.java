package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Address;
import com.botoni.vsr.database.entity.Company;
import com.botoni.vsr.database.entity.Property;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.PropertyEditRequest;
import com.botoni.vsr.dto.request.PropertyRequest;
import com.botoni.vsr.dto.response.AddressResponse;
import com.botoni.vsr.dto.response.PropertyResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfiguration.class)
public interface PropertyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "user", expression = "java(company == null ? user : null)")
    @Mapping(target = "company", source = "company")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "category", source = "request.category")
    Property entity(User user, Company company, Address address, PropertyRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "address", ignore = true)
    void amend(PropertyEditRequest request, @MappingTarget Property property);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "company", source = "company.uuid")
    PropertyResponse response(Property property);

    @Mapping(target = "cep", source = "street.cep")
    @Mapping(target = "state", source = "street.district.city.state.acronym")
    @Mapping(target = "city", source = "street.district.city.name")
    @Mapping(target = "district", source = "street.district.name")
    @Mapping(target = "street", source = "street.name")
    AddressResponse address(Address address);
}
