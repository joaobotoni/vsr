package com.botoni.vsr.bundle.mapper;

import com.botoni.vsr.bundle.LocalCredentialBundle;
import com.botoni.vsr.shared.vo.Password;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.infra.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface LocalCredentialBundleMapper {

    LocalCredentialBundle toBundle(User user, Password password);
}
