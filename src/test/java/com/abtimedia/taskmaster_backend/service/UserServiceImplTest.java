package com.abtimedia.taskmaster_backend.service;
import com.abtimedia.taskmaster_backend.entity.Role;
import com.abtimedia.taskmaster_backend.entity.User;
import com.abtimedia.taskmaster_backend.repository.IUserRepository;
import com.abtimedia.taskmaster_backend.repository.IUserRoleRepository;
import com.abtimedia.taskmaster_backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private IUserRepository userRepo;
    private IUserRoleRepository urRepo;
    private PasswordEncoder encoder;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepo = mock(IUserRepository.class);
        urRepo = mock(IUserRoleRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new UserServiceImpl(userRepo, urRepo, encoder);
    }

    @Test
    void updateTransactional_sinPassword_conservaHash() throws Exception {
        UUID id = UUID.randomUUID();

        // usuario en BD con hash existente
        User existing = new User();
        existing.setIdUser(id);
        existing.setFullName("Nombre Viejo");
        existing.setEmail("old@example.com");
        existing.setPassword("$2a$10$EXISTINGHASH....");
        existing.setStatus(1);

        when(userRepo.findById(id)).thenReturn(Optional.of(existing));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // incoming SIN password
        User incoming = new User();
        incoming.setFullName("Nombre Nuevo");
        incoming.setEmail("new@example.com");
        incoming.setStatus(1);

        // roles con UUID (no int)
        Role r1 = new Role(); r1.setIdRole(UUID.randomUUID());
        Role r2 = new Role(); r2.setIdRole(UUID.randomUUID());

        User saved = service.updateTransactional(id, incoming, List.of(r1, r2));

        assertEquals("Nombre Nuevo", saved.getFullName());
        assertEquals("new@example.com", saved.getEmail());
        assertEquals("$2a$10$EXISTINGHASH....", saved.getPassword());

        verify(encoder, never()).encode(any());
        verify(urRepo).softDeleteByUserId(id);
        verify(urRepo).saveRole(id, r1.getIdRole());
        verify(urRepo).saveRole(id, r2.getIdRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        assertEquals("$2a$10$EXISTINGHASH....", captor.getValue().getPassword());
    }

    @Test
    void updateTransactional_conNuevaPassword_rehash_OK() throws Exception {
        UUID id = UUID.randomUUID();

        User existing = new User();
        existing.setIdUser(id);
        existing.setFullName("Nombre Viejo");
        existing.setEmail("old@example.com");
        existing.setPassword("$2a$10$EXISTINGHASH....");
        existing.setStatus(1);

        when(userRepo.findById(id)).thenReturn(Optional.of(existing));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // incoming CON password en texto
        User incoming = new User();
        incoming.setFullName("Nombre Nuevo");
        incoming.setEmail("new@example.com");
        incoming.setPassword("nuevaClave123");
        incoming.setStatus(1);

        Role r1 = new Role(); r1.setIdRole(UUID.randomUUID());

        when(encoder.encode("nuevaClave123")).thenReturn("$2a$10$NUEVOHASH....");

        User saved = service.updateTransactional(id, incoming, List.of(r1));

        assertEquals("Nombre Nuevo", saved.getFullName());
        assertEquals("new@example.com", saved.getEmail());
        assertEquals("$2a$10$NUEVOHASH....", saved.getPassword());

        verify(encoder).encode("nuevaClave123");
        verify(urRepo).softDeleteByUserId(id);
        verify(urRepo).saveRole(id, r1.getIdRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        assertEquals("$2a$10$NUEVOHASH....", captor.getValue().getPassword());
    }
}