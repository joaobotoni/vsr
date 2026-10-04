package com.botoni.vsr.mapper.session;

import com.botoni.vsr.command.session.AccessCommand;
import com.botoni.vsr.command.session.SessionCommand;
import com.botoni.vsr.mapper.configuration.MapperConfiguration;
import com.botoni.vsr.security.Principal;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccessMapper {
    AccessCommand toCommand(Principal principal, SessionCommand session);
}
