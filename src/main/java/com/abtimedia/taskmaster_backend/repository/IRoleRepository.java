package com.abtimedia.taskmaster_backend.repository;


import com.abtimedia.taskmaster_backend.entity.Role;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IRoleRepository extends IGenericRepository<Role, UUID> {
}
