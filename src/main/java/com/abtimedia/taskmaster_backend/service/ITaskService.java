package com.abtimedia.taskmaster_backend.service;

import com.abtimedia.taskmaster_backend.entity.Task;

import java.util.List;
import java.util.UUID;

public interface ITaskService extends IGenericService<Task, UUID>{
    List<Task> findByUser(UUID idUser) throws Exception;
}
