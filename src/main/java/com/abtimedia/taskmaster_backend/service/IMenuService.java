package com.abtimedia.taskmaster_backend.service;



import com.abtimedia.taskmaster_backend.entity.Menu;

import java.util.List;

public interface IMenuService {
    List<Menu> getMenusByUsername(String username);
}
