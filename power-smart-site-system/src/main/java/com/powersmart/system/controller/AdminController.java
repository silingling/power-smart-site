package com.powersmart.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.JwtUtil;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.common.util.RedisUtil;
import com.powersmart.system.entity.*;
import com.powersmart.system.mapper.*;
import com.powersmart.system.service.SysRoleService;
import com.powersmart.system.service.impl.SysMenuServiceImpl;
import com.powersmart.system.service.impl.SysUserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统管理 — 登录/用户/部门/业务类型
 */
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final JwtUtil jwtUtil;
    private final SysUserServiceImpl userService;
    private final SysRoleService roleService;
    private final SysMenuServiceImpl menuService;
    private final UserRoleMapper userRoleMapper;

    // ==================== 登录 ====================

    @PostMapping("/login")
    @OperateLog(module = "系统管理", action = "login", description = "用户登录", recordParams = false)
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password))
            return Result.fail("用户名或密码不能为空");

        try {
            SysUser user = userService.login(username, password);

            if (user.getStatus() == 0)
                return Result.fail("账号已禁用");

            // 生成 JWT token（含权限列表）
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), permissions);

            // 查询角色和权限
            List<SysRole> roles = roleService.getRolesByUserId(user.getId());
            List<String> roleKeys = roles.stream().map(SysRole::getRoleKey).collect(Collectors.toList());
            List<String> permissions = roleService.getUserPermissions(user.getId());

            // 构建菜单树
            List<SysMenu> userMenus = roleService.getUserMenus(user.getId());
            List<Map<String, Object>> menuTree = menuService.buildTree(userMenus)
                    .stream()
                    .filter(m -> m.getMenuType() != null && m.getMenuType() <= 2)
                    .map(this::menuToMap)
                    .collect(Collectors.toList());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("deptParentId", "1");
            result.put("roles", roleKeys);
            result.put("permissions", permissions);
            result.put("menus", menuTree);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("用户名或密码错误");
        }
    }

    @PostMapping("/logout")
    @OperateLog(module = "系统管理", action = "logout", description = "用户登出")
    public Result<Void> logout(@RequestHeader(value = "Admin-Token", required = false) String token) {
        if (StrUtil.isNotBlank(token)) {
            RedisUtil.addToBlacklist(token);
        }
        return Result.ok();
    }

    // ==================== adminUser ====================

    @PostMapping("/adminUser/querySystemStatus")
    public Result<Map<String, Object>> querySystemStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("totalUser", userMapper.selectCount(null));
        status.put("systemName", "山西同业电力智慧工地");
        status.put("version", "1.0.0");
        return Result.ok(status);
    }

    @PostMapping("/adminUser/initUser")
    public Result<Map<String, Object>> initUser() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("userId", 1);
        info.put("username", "admin");
        info.put("realName", "管理员");
        info.put("deptParentId", "1");
        return Result.ok(info);
    }

    @PostMapping("/adminUser/queryUserList")
    public Result<PageResult<Map<String, Object>>> queryUserList(@RequestBody(required = false) Map<String, Object> params) {
        int pageNum = 1, pageSize = 50;
        if (params != null) {
            if (params.containsKey("page")) pageNum = safeParseInt(params.get("page"), 1);
            if (params.containsKey("pageSize")) pageSize = safeParseInt(params.get("pageSize"), 50);
            if (params.containsKey("limit")) pageSize = safeParseInt(params.get("limit"), 50);
        }
        // pageSize 安全上限
        pageSize = Math.min(pageSize, 200);

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (params != null && params.containsKey("realName") && params.get("realName") != null)
            wrapper.like(SysUser::getRealName, params.get("realName").toString());

        List<SysUser> users = userMapper.selectPage(page, wrapper).getRecords();
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysUser u : users) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getRealName());
            m.put("phone", u.getPhone());
            // 不返回密码到前端
            list.add(m);
        }
        return Result.ok(PageResult.of(list, page.getTotal(), pageNum, pageSize));
    }

    // ==================== adminCommon ====================

    @PostMapping("/adminCommon/getBType")
    public Result<List<Map<String, Object>>> getBType() {
        List<Map<String, Object>> types = new ArrayList<>();
        Map<String, Object> t1 = new LinkedHashMap<>();
        t1.put("typeId", "1");
        t1.put("typeName", "项目部");
        types.add(t1);
        Map<String, Object> t2 = new LinkedHashMap<>();
        t2.put("typeId", "2");
        t2.put("typeName", "公司总部");
        types.add(t2);
        return Result.ok(types);
    }

    // ==================== adminDept ====================

    @PostMapping("/adminDept/queryDeptTree")
    public Result<List<Map<String, Object>>> queryDeptTree() {
        List<SysDept> depts = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getSortOrder));
        return Result.ok(buildDeptTree(depts, 0L));
    }

    private List<Map<String, Object>> buildDeptTree(List<SysDept> all, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysDept d : all) {
            if (d.getParentId() != null && d.getParentId().equals(parentId)) {
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", d.getId());
                node.put("label", d.getDeptName());
                node.put("deptType", d.getDeptType());
                List<Map<String, Object>> children = buildDeptTree(all, d.getId());
                if (!children.isEmpty()) node.put("children", children);
                tree.add(node);
            }
        }
        return tree;
    }

    /** 安全解析整数，解析失败返回默认值 */
    private int safeParseInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ===== 部门管理 =====
    @PostMapping("/adminDept/deleteDept/{id}")
    public Result<Void> deleteDept(@PathVariable Long id) {
        return Result.ok();
    }

    @PostMapping("/adminDept/setDept")
    public Result<Void> setDept(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }

    @PostMapping("/adminDept/addDept")
    public Result<Void> addDept(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }

    // ===== 用户管理 =====
    @PostMapping("/adminUser/setUser")
    public Result<Void> setUser(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }

    @PostMapping("/adminUser/addUser")
    public Result<Void> addUser(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }

    @GetMapping("/adminUser/delUser/{userId}")
    public Result<Void> delUser(@PathVariable String userId) {
        return Result.ok();
    }

    @PostMapping("/adminUser/resetPassword")
    public Result<Void> resetPassword(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }

    @PostMapping("/adminUser/usernameEdit")
    public Result<Void> usernameEdit(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }

    @PostMapping("/adminUser/usernameEditByManager")
    public Result<Void> usernameEditByManager(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }

    @PostMapping("/adminUser/setUserStatus")
    public Result<Void> setUserStatus(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }

    @PostMapping("/adminUser/excelImport")
    public Result<Map<String, Object>> excelImport(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(Map.of("count", 0));
    }

    @PostMapping("/adminUser/downExcel")
    public Result<Map<String, Object>> downExcel(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(Map.of("url", ""));
    }

    // ===== 用户-角色分配 =====
    @PostMapping("/adminUser/setUserRole")
    public Result<Void> setUserRole(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(params.getOrDefault("userId", 0).toString());
        @SuppressWarnings("unchecked")
        List<Integer> roleIdInts = (List<Integer>) params.getOrDefault("roleIds", new ArrayList<>());
        List<Long> roleIds = roleIdInts.stream().map(Long::valueOf).collect(Collectors.toList());

        // 删除旧关联
        userRoleMapper.deleteByUserId(userId);
        // 写入新关联
        if (CollUtil.isNotEmpty(roleIds)) {
            roleIds.forEach(roleId -> {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            });
            // 同步更新 sys_user.role_ids（兼容旧代码）
            String roleIdsStr = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            userMapper.updateById(
                    new SysUser() {{
                        setId(userId);
                        setRoleIds(roleIdsStr);
                    }});
        }
        return Result.ok();
    }

    @PostMapping("/adminUser/getUserRole")
    public Result<Map<String, Object>> getUserRole(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(params.getOrDefault("userId", 0).toString());
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        List<SysRole> allRoles = roleService.lambdaQuery()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSortOrder).list();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("roleIds", roleIds);
        result.put("allRoles", allRoles.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("roleId", r.getId());
            m.put("roleName", r.getRoleName());
            m.put("roleKey", r.getRoleKey());
            return m;
        }).collect(Collectors.toList()));
        return Result.ok(result);
    }

    // ===== 用户获取自己的菜单 + 权限 =====
    @PostMapping("/adminUser/getUserPermissions")
    public Result<Map<String, Object>> getUserPermissions(@RequestBody(required = false) Map<String, Object> params) {
        // 从上下文获取当前用户（简化：从 Admin-Token 解析，暂简化）
        // 这里假设前端会在 header 传递 userId 或由 filter 填充
        // 后端在 Phase G 中会完善
        return Result.ok(new LinkedHashMap<>());
    }

    // ===== 帮助方法 =====

    /** SysMenu → 前端标准 Map */
    private Map<String, Object> menuToMap(SysMenu menu) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", menu.getId());
        m.put("parentId", menu.getParentId());
        m.put("name", menu.getName());
        m.put("permissionKey", menu.getPermissionKey() != null ? menu.getPermissionKey() : "");
        m.put("path", menu.getPath() != null ? menu.getPath() : "");
        m.put("icon", menu.getIcon() != null ? menu.getIcon() : "");
        m.put("menuType", menu.getMenuType());
        m.put("visible", menu.getVisible());
        m.put("sortOrder", menu.getSortOrder());
        if (CollUtil.isNotEmpty(menu.getChildren())) {
            m.put("children", menu.getChildren().stream()
                    .filter(c -> c.getMenuType() != null && c.getMenuType() <= 2)
                    .map(this::menuToMap)
                    .collect(Collectors.toList()));
        }
        return m;
    }
}
