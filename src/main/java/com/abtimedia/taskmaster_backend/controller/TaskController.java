package com.abtimedia.taskmaster_backend.controller;

import com.abtimedia.taskmaster_backend.dto.TaskDTO;
import com.abtimedia.taskmaster_backend.entity.Task;
import com.abtimedia.taskmaster_backend.service.ITaskService;
import com.abtimedia.taskmaster_backend.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    public  final ITaskService taskService;
    private final MapperUtil mapperUtil;

    @PreAuthorize("@authorizeLogic.hasAccess('findAll')")
    @GetMapping
    public ResponseEntity<List<TaskDTO>> findAll() throws Exception {
        List<TaskDTO> list = mapperUtil.mapList(taskService.findAll(), TaskDTO.class);

        return ResponseEntity.ok(list);
    }

    @PreAuthorize("@authorizeLogic.hasAccess('findByUserTasks')")
    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<TaskDTO>> findByUser(@PathVariable UUID idUser) throws Exception {
        List<Task> tasks = taskService.findByUser(idUser);
        List<TaskDTO> dto = mapperUtil.mapList(tasks, TaskDTO.class);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("@authorizeLogic.hasAccess('findById')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> findById(@PathVariable("id") UUID id) throws Exception {
        TaskDTO obj = mapperUtil.map(taskService.findById(id), TaskDTO.class);
        return ResponseEntity.ok(obj);
    }

    @PreAuthorize("@authorizeLogic.hasAccess('save')")
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody TaskDTO taskDTO) throws Exception{
        Task obj = taskService.save(mapperUtil.map(taskDTO, Task.class));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(obj.getIdTask()).toUri();

        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@authorizeLogic.hasAccess('updateTask')")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable("id") UUID id, @RequestBody TaskDTO taskDTO) throws Exception{
        Task obj = taskService.update(mapperUtil.map(taskDTO, Task.class), id);

        return ResponseEntity.ok(mapperUtil.map(obj, TaskDTO.class));
    }

    @PreAuthorize("@authorizeLogic.hasAccess('deleteTask')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) throws Exception{
        taskService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
