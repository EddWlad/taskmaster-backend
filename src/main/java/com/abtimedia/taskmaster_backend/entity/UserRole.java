package com.abtimedia.taskmaster_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserRolePK.class)
public class UserRole {
    @Id
    private User user;

    @Id
    private Role role;

    private Integer status = 1;
}
