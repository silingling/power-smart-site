package com.powersmart.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.system.entity.SysMenu;
import com.powersmart.system.entity.SysRole;
import com.powersmart.system.entity.SysRoleMenu;
import com.powersmart.system.mapper.*;
import com.powersmart.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final UserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (CollUtil.isEmpty(roleIds)) return Collections.emptyList();
        return list(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 删除旧关联
        roleMenuMapper.deleteByRoleId(roleId);
        // 批量插入新关联
        if (CollUtil.isNotEmpty(menuIds)) {
            List<SysRoleMenu> list = menuIds.stream()
                    .map(mid -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(mid);
                        return rm;
                    })
                    .collect(Collectors.toList());
            // 批量插入
            list.forEach(roleMenuMapper::insert);
        }
    }

    @Override
    public List<Long> getAssignedMenuIds(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (CollUtil.isEmpty(roleIds)) return Collections.emptyList();
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
        if (CollUtil.isEmpty(menuIds)) return Collections.emptyList();
        // 查询启用的可见菜单
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 1)
                .eq(SysMenu::getIsDeleted, 0)
                .orderByAsc(SysMenu::getSortOrder));
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (CollUtil.isEmpty(roleIds)) return Collections.emptyList();
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
        if (CollUtil.isEmpty(menuIds)) return Collections.emptyList();
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 1)
                .isNotNull(SysMenu::getPermissionKey)
                .ne(SysMenu::getPermissionKey, ""));
        return menus.stream()
                .map(SysMenu::getPermissionKey)
                .collect(Collectors.toList());
    }
}
