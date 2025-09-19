package com.abtimedia.taskmaster_backend.service.impl;


import com.abtimedia.taskmaster_backend.entity.Role;
import com.abtimedia.taskmaster_backend.entity.User;
import com.abtimedia.taskmaster_backend.repository.IGenericRepository;
import com.abtimedia.taskmaster_backend.repository.IUserRepository;
import com.abtimedia.taskmaster_backend.repository.IUserRoleRepository;
import com.abtimedia.taskmaster_backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends GenericServiceImpl<User, UUID> implements IUserService {

    private final IUserRepository userRepository;
    private final IUserRoleRepository urRepo;


    @Override
    protected IGenericRepository<User, UUID> getRepo() {
        return userRepository;
    }

    @Override
    public User findOneByEmail(String email) {
        return userRepository.findOneByEmail(email);
    }

    @Override
    public void changePassword(String username, String newPassword) {
        //userRepository.changePassword(username, bcrypt.encode(newPassword)) ;
    }

    @Transactional
    @Override
    public User saveTransactional(User user, List<Role> roles) throws Exception {
        User optionalUser = userRepository.findOneByEmail(user.getEmail());
        if(optionalUser != null) {
            User existingUser = optionalUser;
            if (existingUser.getStatus() == 0) {
                // Reactivate and update user data
                existingUser.setStatus(1);
                existingUser.setFullName(user.getFullName());
                existingUser.setEmail(user.getEmail());
                existingUser.setDateCreate(LocalDateTime.now());
                existingUser.setPassword(user.getPassword());
                userRepository.save(existingUser);

                // Clear and reassign roles
                urRepo.deleteByUserId(existingUser.getIdUser());
                roles.forEach(role -> urRepo.saveRole(existingUser.getIdUser(), role.getIdRole()));

                return existingUser;
            } else {
                throw new RuntimeException("A user with this identification already exists");
            }
        }
        // Create new user
        userRepository.save(user);
        roles.forEach(role -> urRepo.saveRole(user.getIdUser(), role.getIdRole()));

        return user;

    }

    @Transactional
    @Override
    public User updateTransactional(UUID id, User user, List<Role> roles) throws Exception {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID NOT FOUND " + id));

        if (existingUser.getStatus() == 0) {
            throw new RuntimeException("The user was logically deleted. Cannot update.");
        }

        // Update basic data
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setStatus(user.getStatus());

        userRepository.save(existingUser);

        // Clear and reassign roles
        urRepo.softDeleteByUserId(id);
        roles.forEach(role -> urRepo.saveRole(id, role.getIdRole()));

        return existingUser;
    }

    @Override
    public User findUserDetail(UUID id) throws Exception {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> findActiveUsersByRole(String roleName) throws Exception {
        return userRepository.findUsersByRoleNameAndStatus(roleName);
    }

    @Transactional
    @Override
    public boolean softDeleteTransactional(UUID id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID NOT FOUND " + id));

        // Soft delete the user
        user.setStatus(0);
        userRepository.save(user);

        // Soft delete the roles associated with the user
        urRepo.softDeleteByUserId(id);

        return true;
    }
}