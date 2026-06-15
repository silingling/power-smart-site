package com.powersmart.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powersmart.system.entity.SysMenu;
import com.powersmart.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    /** 根据用户 ID 查询所有角色 */
    List<SysRole> getRolesByUserId(Long userId);

    /** 为角色分配菜单（全量替换） */
    void assignMenus(Long roleId, List<Long> menuIds);

    /** 查询角色已分配的菜单 ID 列表 */
    List<Long> getAssignedMenuIds(Long roleId);

    /** 查询用户拥有的所有菜单（去重、可见、启用） */
    List<SysMenu> getUserMenus(Long userId);

    /** 查询用户拥有的所有权限标识符（permission_key） */
    List<String> getUserPermissions(Long userId);
}
