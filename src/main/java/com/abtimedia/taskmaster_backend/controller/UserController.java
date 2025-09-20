package com.abtimedia.taskmaster_backend.controller;


import com.abtimedia.taskmaster_backend.dto.RoleDTO;
import com.abtimedia.taskmaster_backend.dto.UserDTO;
import com.abtimedia.taskmaster_backend.dto.UserDetailDTO;
import com.abtimedia.taskmaster_backend.dto.UserListRoleDTO;
import com.abtimedia.taskmaster_backend.entity.Role;
import com.abtimedia.taskmaster_backend.entity.User;
import com.abtimedia.taskmaster_backend.repository.IUserRoleRepository;
import com.abtimedia.taskmaster_backend.service.IUserService;
import com.abtimedia.taskmaster_backend.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final MapperUtil mapperUtil;
    private final IUserRoleRepository urRepo;

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() throws Exception {
        List<UserDTO> list = mapperUtil.mapList(userService.findAll(), UserDTO.class);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailDTO> findUserDetail(@PathVariable("id") String id) throws Exception{
        UUID userId = UUID.fromString(id);
        User user = userService.findUserDetail(userId);

        UserDetailDTO detail = new UserDetailDTO();
        detail.setUser(mapperUtil.map(user, UserDTO.class));

        List<RoleDTO> roles = urRepo.findActiveRolesByUserId(userId).stream()
                .map(ur -> mapperUtil.map(ur.getRole(), RoleDTO.class))
                .toList();
        detail.setRoles(roles);

        return ResponseEntity.ok(detail);
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody UserListRoleDTO dto) throws Exception{
        User obj1 = mapperUtil.map(dto.getUser(), User.class);
        List<Role> list = mapperUtil.mapList(dto.getRoles(), Role.class);

        User obj = userService.saveTransactional(obj1, list);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(obj.getIdUser())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable("id") String id, @RequestBody UserListRoleDTO dto) throws Exception{
        User user = mapperUtil.map(dto.getUser(), User.class);
        List<Role> roles = mapperUtil.mapList(dto.getRoles(), Role.class);

        User obj = userService.updateTransactional(UUID.fromString(id), user, roles);

        return ResponseEntity.ok(mapperUtil.map(obj, UserDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) throws Exception{
        userService.softDeleteTransactional(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserDTO>> findActiveLawyers() throws Exception {
        List<User> list = userService.findActiveUsersByRole("ADMINISTRADOR");
        return ResponseEntity.ok(mapperUtil.mapList(list, UserDTO.class));
    }

    @GetMapping("/developers")
    public ResponseEntity<List<UserDTO>> findActiveClients() throws Exception {
        List<User> list = userService.findActiveUsersByRole("DESARROLLADOR");
        return ResponseEntity.ok(mapperUtil.mapList(list, UserDTO.class));
    }
}
