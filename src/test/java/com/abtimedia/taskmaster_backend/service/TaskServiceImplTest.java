package com.abtimedia.taskmaster_backend.service;

import com.abtimedia.taskmaster_backend.entity.Task;
import com.abtimedia.taskmaster_backend.entity.TaskStatus;
import com.abtimedia.taskmaster_backend.entity.User;
import com.abtimedia.taskmaster_backend.exception.ModelNotFoundException;
import com.abtimedia.taskmaster_backend.repository.ITaskRepository;
import com.abtimedia.taskmaster_backend.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceImplTest {

    private ITaskRepository taskRepo;
    private IUserService userService;
    private TaskServiceImpl service;

    @BeforeEach
    void setUp() {
        taskRepo = mock(ITaskRepository.class);
        userService = mock(IUserService.class);

        service = new TaskServiceImpl(taskRepo, userService);

        var auth = new UsernamePasswordAuthenticationToken(
                "dev@example.com",
                "x",
                List.of(new SimpleGrantedAuthority("ROLE_DESARROLLADOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void update_preserva_taskDateCreate_y_owner_para_dev_duenho() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var me = new User();
        me.setIdUser(userId);
        me.setEmail("dev@example.com");
        me.setStatus(1);

        var db = new Task();
        db.setIdTask(taskId);
        db.setTaskTitle("Old Title");
        db.setTaskDescription("Old Desc");
        db.setTaskStatus(TaskStatus.PENDIENTE);
        db.setTaskDateCreate(LocalDateTime.now().minusDays(1));
        db.setStatus(1);
        var owner = new User();
        owner.setIdUser(userId);
        db.setUser(owner);

        when(userService.findOneByEmail("dev@example.com")).thenReturn(me);
        when(taskRepo.findByIdTaskAndStatusNot(taskId, 0)).thenReturn(Optional.of(db));
        when(taskRepo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        var incoming = new Task();
        incoming.setTaskTitle("New Title");
        incoming.setTaskDescription("New Desc");
        incoming.setTaskStatus(TaskStatus.EN_PROGRESO);

        Task saved = service.update(incoming, taskId);

        assertEquals(db.getTaskDateCreate(), saved.getTaskDateCreate());
        assertNotNull(saved.getUser());
        assertEquals(userId, saved.getUser().getIdUser());
        assertEquals("New Title", saved.getTaskTitle());
        assertEquals(TaskStatus.EN_PROGRESO, saved.getTaskStatus());

        var captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepo).save(captor.capture());
        assertEquals(db.getTaskDateCreate(), captor.getValue().getTaskDateCreate());
    }

    @Test
    void update_otro_owner_para_dev_lanza_403() throws Exception {
        UUID taskId = UUID.randomUUID();

        var me = new User();
        me.setIdUser(UUID.randomUUID());
        me.setEmail("dev@example.com");
        me.setStatus(1);

        var db = new Task();
        db.setIdTask(taskId);
        var otherOwner = new User();
        otherOwner.setIdUser(UUID.randomUUID());
        db.setUser(otherOwner);
        db.setStatus(1);

        when(userService.findOneByEmail("dev@example.com")).thenReturn(me);
        when(taskRepo.findByIdTaskAndStatusNot(taskId, 0)).thenReturn(Optional.of(db));

        assertThrows(AuthorizationDeniedException.class, () -> service.update(new Task(), taskId));
        verify(taskRepo, never()).save(any());
    }

    @Test
    void update_task_no_existe_lanza_404() throws Exception {
        UUID taskId = UUID.randomUUID();

        var me = new User();
        me.setIdUser(UUID.randomUUID());
        me.setEmail("dev@example.com");
        me.setStatus(1);

        when(userService.findOneByEmail("dev@example.com")).thenReturn(me); // <<-- CAMBIO
        when(taskRepo.findByIdTaskAndStatusNot(taskId, 0)).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> service.update(new Task(), taskId));
        verify(taskRepo, never()).save(any());
    }

    @Test
    void delete_devDuenho_softDelete_ok() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User me = new User();
        me.setIdUser(userId);
        me.setEmail("dev@example.com");
        me.setStatus(1);


        Task db = new Task();
        db.setIdTask(taskId);
        db.setStatus(1);
        User owner = new User(); owner.setIdUser(userId);
        db.setUser(owner);

        when(userService.findOneByEmail("dev@example.com")).thenReturn(me);
        when(taskRepo.findByIdTaskAndStatusNot(taskId, 0)).thenReturn(Optional.of(db));
        when(taskRepo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(taskId);


        verify(taskRepo).save(argThat(t -> t.getIdTask().equals(taskId) && t.getStatus() == 0));
    }

    @Test
    void delete_devNoDuenho_lanza403_y_noGuarda() throws Exception {
        UUID taskId = UUID.randomUUID();

        User me = new User();
        me.setIdUser(UUID.randomUUID());
        me.setEmail("dev@example.com");
        me.setStatus(1);

        Task db = new Task();
        db.setIdTask(taskId);
        db.setStatus(1);
        User other = new User(); other.setIdUser(UUID.randomUUID());
        db.setUser(other);

        when(userService.findOneByEmail("dev@example.com")).thenReturn(me);
        when(taskRepo.findByIdTaskAndStatusNot(taskId, 0)).thenReturn(Optional.of(db));

        assertThrows(AuthorizationDeniedException.class, () -> service.delete(taskId));
        verify(taskRepo, never()).save(any());
    }
}