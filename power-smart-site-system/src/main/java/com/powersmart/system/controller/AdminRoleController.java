package com.powersmart.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.system.entity.SysRole;
import com.powersmart.system.entity.SysUser;
import com.powersmart.system.mapper.SysUserMapper;
import com.powersmart.system.mapper.UserRoleMapper;
import com.powersmart.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理 — 同业电力前端 build/adminRole/*
 *
 * 各端点命名与网关路由 /adminRole/** 对齐。
 */
@RestController
@RequestMapping("/adminRole")
@RequiredArgsConstructor
public class AdminRoleController {

    private final SysRoleService roleService;
    private final SysUserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    /** 查询所有角色列表（前端角色选择器） */
    @PostMapping("/getAllRoleList")
    public Result<List<Map<String, Object>>> getAllRoleList(@RequestBody(required = false) Map<String, Object> params) {
        List<SysRole> roles = roleService.lambdaQuery()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSortOrder)
                .list();
        List<Map<String, Object>> list = roles.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("roleId", r.getId());
            m.put("roleName", r.getRoleName());
            m.put("roleKey", r.getRoleKey());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(list);
    }

    /** 分页查询角色列表 */
    @PostMapping("/queryRoleList")
    public Result<PageResult<Map<String, Object>>> queryRoleList(@RequestBody(required = false) Map<String, Object> params) {
        Page<SysRole> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getSortOrder);

        if (params != null && params.containsKey("roleName") && params.get("roleName") != null) {
            wrapper.like(SysRole::getRoleName, params.get("roleName").toString());
        }

        Page<SysRole> page = roleService.page(pageParam, wrapper);
        List<Map<String, Object>> list = page.getRecords().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("roleId", r.getId());
            m.put("roleName", r.getRoleName());
            m.put("roleKey", r.getRoleKey());
            m.put("status", r.getStatus());
            m.put("sortOrder", r.getSortOrder());
            m.put("remark", r.getRemark());
            // 统计角色下用户数
            List<Long> userIds = userRoleMapper.selectList(
                    new LambdaQueryWrapper<com.powersmart.system.entity.UserRole>()
                            .eq(com.powersmart.system.entity.UserRole::getRoleId, r.getId())
            ).stream().map(com.powersmart.system.entity.UserRole::getUserId).collect(Collectors.toList());
            m.put("userCount", CollUtil.isNotEmpty(userIds) ? userIds.size() : 0);
            return m;
        }).collect(Collectors.toList());

        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /** 创建角色 */
    @PostMapping("/addRole")
    public Result<Void> addRole(@RequestBody Map<String, Object> params) {
        String roleName = params.getOrDefault("roleName", "").toString();
        String roleKey = params.getOrDefault("roleKey", "").toString();
        if (StrUtil.isBlank(roleName) || StrUtil.isBlank(roleKey)) {
            return Result.fail("角色名称和标识不能为空");
        }
        // 检查重复
        long count = roleService.lambdaQuery().eq(SysRole::getRoleKey, roleKey).count();
        if (count > 0) return Result.fail("角色标识已存在");

        SysRole role = new SysRole();
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setStatus(1);
        role.setSortOrder(params.containsKey("sortOrder")
                ? Integer.parseInt(params.get("sortOrder").toString()) : 0);
        role.setRemark(params.getOrDefault("remark", "").toString());
        roleService.save(role);
        return Result.ok();
    }

    /** 更新角色 */
    @PostMapping("/setRole")
    public Result<Void> setRole(@RequestBody Map<String, Object> params) {
        if (!params.containsKey("roleId")) return Result.fail("roleId 不能为空");
        Long roleId = Long.parseLong(params.get("roleId").toString());
        SysRole role = roleService.getById(roleId);
        if (role == null) return Result.fail("角色不存在");

        if (params.containsKey("roleName")) role.setRoleName(params.get("roleName").toString());
        if (params.containsKey("roleKey")) role.setRoleKey(params.get("roleKey").toString());
        if (params.containsKey("sortOrder")) role.setSortOrder(Integer.parseInt(params.get("sortOrder").toString()));
        if (params.containsKey("remark")) role.setRemark(params.get("remark").toString());
        if (params.containsKey("status")) role.setStatus(Integer.parseInt(params.get("status").toString()));
        roleService.updateById(role);
        return Result.ok();
    }

    /** 删除角色 */
    @PostMapping("/deleteRole/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        if (id == 1) return Result.fail("不能删除超级管理员角色");
        roleService.removeById(id);
        // 同步删除关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<com.powersmart.system.entity.UserRole>()
                        .eq(com.powersmart.system.entity.UserRole::getRoleId, id));
        return Result.ok();
    }

    /** 启用/禁用角色 */
    @PostMapping("/setRoleStatus")
    public Result<Void> setRoleStatus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.parseLong(params.getOrDefault("roleId", 0).toString());
        Integer status = Integer.parseInt(params.getOrDefault("status", 1).toString());
        roleService.lambdaUpdate().eq(SysRole::getId, roleId).set(SysRole::getStatus, status).update();
        return Result.ok();
    }

    /** 查询角色已分配的菜单 ID */
    @PostMapping("/getRoleMenu")
    public Result<Map<String, Object>> getRoleMenu(@RequestBody Map<String, Object> params) {
        Long roleId = Long.parseLong(params.getOrDefault("roleId", 0).toString());
        List<Long> menuIds = roleService.getAssignedMenuIds(roleId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roleId", roleId);
        result.put("menuIds", menuIds);
        return Result.ok(result);
    }

    /** 为角色分配菜单 */
    @PostMapping("/setRoleMenu")
    public Result<Void> setRoleMenu(@RequestBody Map<String, Object> params) {
        Long roleId = Long.parseLong(params.getOrDefault("roleId", 0).toString());
        @SuppressWarnings("unchecked")
        List<Integer> menuIdInts = (List<Integer>) params.getOrDefault("menuIds", new ArrayList<>());
        List<Long> menuIds = menuIdInts.stream().map(Long::valueOf).collect(Collectors.toList());
        roleService.assignMenus(roleId, menuIds);
        return Result.ok();
    }

    /** 查询角色下的用户列表 */
    @PostMapping("/queryRoleUserList")
    public Result<PageResult<Map<String, Object>>> queryRoleUserList(@RequestBody Map<String, Object> params) {
        Long roleId = Long.parseLong(params.getOrDefault("roleId", 0).toString());
        Page<SysUser> pageParam = PageHelper.of(params);

        // 查询角色下的用户 ID
        List<Long> userIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<com.powersmart.system.entity.UserRole>()
                        .eq(com.powersmart.system.entity.UserRole::getRoleId, roleId)
        ).stream().map(com.powersmart.system.entity.UserRole::getUserId).collect(Collectors.toList());

        if (CollUtil.isEmpty(userIds)) {
            return Result.ok(PageResult.of(new ArrayList<>(), 0, (int) pageParam.getCurrent(), (int) pageParam.getSize()));
        }

        Page<SysUser> page = userMapper.selectPage(pageParam,
                new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds));
        List<Map<String, Object>> list = page.getRecords().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getRealName());
            m.put("phone", u.getPhone());
            m.put("status", u.getStatus());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }
}
