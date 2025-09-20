package com.abtimedia.taskmaster_backend.service.impl;

import com.abtimedia.taskmaster_backend.entity.Task;
import com.abtimedia.taskmaster_backend.entity.User;
import com.abtimedia.taskmaster_backend.exception.ModelNotFoundException;
import com.abtimedia.taskmaster_backend.repository.IGenericRepository;
import com.abtimedia.taskmaster_backend.repository.ITaskRepository;
import com.abtimedia.taskmaster_backend.service.ITaskService;
import com.abtimedia.taskmaster_backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends GenericServiceImpl<Task, UUID> implements ITaskService {
    private final ITaskRepository taskRepository;
    private final IUserService userService;

    @Override
    protected IGenericRepository<Task, UUID> getRepo() {
        return taskRepository;
    }

    @Override
    public List<Task> findByUser(UUID idUser) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthorizationDeniedException("Access Denied");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equalsIgnoreCase("ROLE_ADMINISTRADOR"));
        String email = auth.getName();
        User me = userService.findOneByEmail(email);
        if (me == null || (me.getStatus() != null && me.getStatus() == 0)) {
            throw new ModelNotFoundException("Authenticated user not found: " + email);
        }
        if (!isAdmin && !me.getIdUser().equals(idUser)) {
            throw new AuthorizationDeniedException("Access Denied");
        }
        return taskRepository.findByUserIdUserAndStatusNot(idUser, 0);
    }

    @Transactional
    @Override
    public Task update(Task incoming, UUID id) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthorizationDeniedException("Access Denied");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equalsIgnoreCase("ROLE_ADMINISTRADOR"));
        String email = auth.getName();
        User me = userService.findOneByEmail(email);
        if (me == null || (me.getStatus() != null && me.getStatus() == 0)) {
            throw new ModelNotFoundException("Authenticated user not found: " + email);
        }
        Task db = taskRepository.findByIdTaskAndStatusNot(id, 0)
                .orElseThrow(() -> new ModelNotFoundException("Task not found: " + id));

        if (!isAdmin && !db.getUser().getIdUser().equals(me.getIdUser())) {
            throw new AuthorizationDeniedException("Access Denied");
        }
        incoming.setIdTask(db.getIdTask());
        incoming.setTaskDateCreate(db.getTaskDateCreate());
        incoming.setUser(db.getUser());
        if (incoming.getStatus() == null) {
            incoming.setStatus(db.getStatus());
        }
        return taskRepository.save(incoming);
    }

    @Transactional
    @Override
    public boolean delete(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthorizationDeniedException("Access Denied");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equalsIgnoreCase("ROLE_ADMINISTRADOR"));
        String email = auth.getName();
        User me = userService.findOneByEmail(email);
        if (me == null || (me.getStatus() != null && me.getStatus() == 0)) {
            throw new ModelNotFoundException("Authenticated user not found: " + email);
        }
        Task db = taskRepository.findByIdTaskAndStatusNot(id, 0)
                .orElseThrow(() -> new ModelNotFoundException("Task not found: " + id));
        if (!isAdmin && !db.getUser().getIdUser().equals(me.getIdUser())) {
            throw new AuthorizationDeniedException("Access Denied");
        }
        db.setStatus(0);
        taskRepository.save(db);
        return true;
    }
}
