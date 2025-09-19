package com.abtimedia.taskmaster_backend.service;


import com.abtimedia.taskmaster_backend.entity.Role;
import com.abtimedia.taskmaster_backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface IUserService extends IGenericService<User, UUID>{
    User findOneByEmail(String email);
    void changePassword(String email, String newPassword);
    
    User saveTransactional(User user, List<Role> roles) throws Exception;
    User updateTransactional(UUID id, User user, List<Role> roles) throws Exception;
    User findUserDetail(UUID id) throws Exception;

    List<User> findActiveUsersByRole(String roleName) throws Exception;
}
