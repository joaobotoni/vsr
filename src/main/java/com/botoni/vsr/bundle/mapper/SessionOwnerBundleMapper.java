package com.botoni.vsr.bundle.mapper;

import com.botoni.vsr.bundle.AccessBundle;
import com.botoni.vsr.bundle.SessionOwnerBundle;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface SessionOwnerBundleMapper {

    @Mapping(target = "user", expression = "java(bundle.principal().user())")
    @Mapping(target = "session", source = "session")
    SessionOwnerBundle toBundle(AccessBundle bundle);
}
