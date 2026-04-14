package com.alvaromg.portfolio.application.users.ports.in;

import lombok.Builder;
import java.util.UUID;

public interface ReadUserAvatarUseCase {

    String readUserAvatar(ReadUserAvatarCommand command);

    @Builder
    record ReadUserAvatarCommand(
        UUID userId
    ) {}
}
