package com.abtimedia.taskmaster_backend.service.impl;


import com.abtimedia.taskmaster_backend.entity.Menu;
import com.abtimedia.taskmaster_backend.repository.IGenericRepository;
import com.abtimedia.taskmaster_backend.repository.IMenuRepository;
import com.abtimedia.taskmaster_backend.service.IMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends GenericServiceImpl<Menu, UUID> implements IMenuService {

    private final IMenuRepository menuRepository;

    @Override
    protected IGenericRepository<Menu, UUID> getRepo() {
        return menuRepository;
    }

    @Override
    public List<Menu> getMenusByUsername(String username) {
        return menuRepository.getMenusByUsername(username);
    }


}