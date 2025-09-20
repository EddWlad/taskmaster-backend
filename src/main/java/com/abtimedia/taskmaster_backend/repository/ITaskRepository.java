package com.abtimedia.taskmaster_backend.repository;

import com.abtimedia.taskmaster_backend.entity.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskRepository extends IGenericRepository<Task, UUID> {
    List<Task> findByUserIdUserAndStatusNot(UUID idUser, Integer status);
    Optional<Task> findByIdTaskAndStatusNot(UUID idTask, Integer status);

}