package com.botoni.vsr.bundle.mapper;

import com.botoni.vsr.auth.dto.request.RegisterRequest;
import com.botoni.vsr.bundle.SessionBundle;
import com.botoni.vsr.bundle.SignUpBundle;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface SignUpBundleMapper {

    SignUpBundle toBundle(RegisterRequest register, SessionBundle session);
}
