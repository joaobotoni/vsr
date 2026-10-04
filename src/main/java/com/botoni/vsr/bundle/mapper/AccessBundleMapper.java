package com.botoni.vsr.bundle.mapper;

import com.botoni.vsr.bundle.AccessBundle;
import com.botoni.vsr.bundle.SessionBundle;
import com.botoni.vsr.infra.security.Principal;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccessBundleMapper {

    AccessBundle toBundle(Principal principal, SessionBundle session);
}
