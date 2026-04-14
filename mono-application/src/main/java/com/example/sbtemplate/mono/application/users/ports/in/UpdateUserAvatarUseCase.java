package com.example.sbtemplate.mono.application.users.ports.in;

import java.util.UUID;
import lombok.Builder;

public interface UpdateUserAvatarUseCase {

    boolean updateUserAvatar(UpdateUserAvatarCommand command);

    @Builder
    record UpdateUserAvatarCommand(
        UUID userId,
        UUID clientId,
        String avatar
    ) {}
}
