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
        User user = userService.findById(idUser);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new ModelNotFoundException("User not found: " + idUser);
        }
        return taskRepository.findByUserIdUserAndStatusNot(idUser, 0);
    }
}
