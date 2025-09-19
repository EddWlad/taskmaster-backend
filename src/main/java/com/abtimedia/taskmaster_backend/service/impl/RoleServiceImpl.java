package com.abtimedia.taskmaster_backend.service.impl;

import com.abtimedia.taskmaster_backend.entity.Role;
import com.abtimedia.taskmaster_backend.repository.IGenericRepository;
import com.abtimedia.taskmaster_backend.repository.IRoleRepository;
import com.abtimedia.taskmaster_backend.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends GenericServiceImpl<Role, UUID> implements IRoleService {

    private final IRoleRepository roleRepository;

    @Override
    protected IGenericRepository<Role, UUID> getRepo() {
        return roleRepository;
    }

}
