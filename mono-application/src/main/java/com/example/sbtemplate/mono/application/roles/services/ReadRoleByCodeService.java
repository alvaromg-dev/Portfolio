package com.example.sbtemplate.mono.application.roles.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sbtemplate.mono.application.roles.exceptions.RoleNotFoundException;
import com.example.sbtemplate.mono.application.roles.ports.in.ReadRoleByCodeUseCase;
import com.example.sbtemplate.mono.application.roles.ports.out.RoleRepository;
import com.example.sbtemplate.mono.domain.model.Role;

@Service
@Transactional(readOnly = true)
public class ReadRoleByCodeService implements ReadRoleByCodeUseCase {

    @Autowired private RoleRepository repository;

    @Override
    public Role getRoleByCode(ReadRoleByCodeCommand command) {
        return repository.findByCode(command.code())
            .orElseThrow(() -> new RoleNotFoundException(command.code()));
    }
}
