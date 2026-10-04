package com.botoni.vsr.bundle.mapper;

import com.botoni.vsr.auth.dto.request.LoginRequest;
import com.botoni.vsr.bundle.SessionBundle;
import com.botoni.vsr.bundle.SignInBundle;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface SignInBundleMapper {

    SignInBundle toBundle(LoginRequest login, SessionBundle session);
}
