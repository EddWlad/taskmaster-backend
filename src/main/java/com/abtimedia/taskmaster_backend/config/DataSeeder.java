package com.abtimedia.taskmaster_backend.config;

import com.abtimedia.taskmaster_backend.entity.Role;
import com.abtimedia.taskmaster_backend.entity.User;
import com.abtimedia.taskmaster_backend.entity.UserRole;
import com.abtimedia.taskmaster_backend.repository.IRoleRepository;
import com.abtimedia.taskmaster_backend.repository.IUserRepository;
import com.abtimedia.taskmaster_backend.repository.IUserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;


    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Bean
    public ApplicationRunner seedRunner() {
        return args -> {
            if (!seedEnabled) return;
            seedRolesAndUsers();
        };
    }

    @Transactional
    protected void seedRolesAndUsers() {
        Role adminRole = findOrCreateRole("ADMINISTRADOR", "Rol con permisos administrativos");
        Role devRole   = findOrCreateRole("DESARROLLADOR", "Rol para gestion y ejecucion de tareas");

        User admin = findOrCreateUser("Ing.Robinson Ortega", "desarrollo@abitmedia.cloud", "admin123");
        User dev   = findOrCreateUser("Ing.Edison Morocho", "desarrollador1@abitmedia.cloud", "dev123");

        linkRoleIfMissing(admin.getIdUser(), adminRole.getIdRole());
        linkRoleIfMissing(dev.getIdUser(),   devRole.getIdRole());
    }

    private Role findOrCreateRole(String roleName, String description) {
        Optional<Role> opt = roleRepository.findByName(roleName);
        if (opt.isPresent()) {
            return opt.get();
        }
        Role r = new Role();
        r.setName(roleName);
        r.setStatus(1);
        r.setDescription(description);
        return roleRepository.save(r);
    }

    private User findOrCreateUser(String fullName, String email, String rawPassword) {
        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isPresent()) {
            return opt.get();
        }
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPassword(rawPassword);
        u.setStatus(1);
        return userRepository.save(u);
    }

    private void linkRoleIfMissing(UUID userId, UUID roleId) {
        boolean exists = userRoleRepository
                .existsByUser_IdUserAndRole_IdRole(userId, roleId);
        if (!exists) {
            UserRole ur = new UserRole();
            User user = userRepository.findById(userId).orElseThrow();
            Role role = roleRepository.findById(roleId).orElseThrow();
            ur.setUser(user);
            ur.setRole(role);
            ur.setStatus(1);
            userRoleRepository.save(ur);
        }
    }
}