package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.auth.JwtUtil;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SysDept;
import com.powersmart.system.entity.SysUser;
import com.powersmart.system.mapper.SysDeptMapper;
import com.powersmart.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统管理 — 登录/用户/部门/业务类型
 */
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final JwtUtil jwtUtil;

    // ==================== 登录 ====================

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password))
            return Result.fail("用户名或密码不能为空");

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null)
            return Result.fail("用户名或密码错误");

        // MD5 校验（与 SysUserServiceImpl 保持一致）
        String hashed = DigestUtil.md5Hex(password);

        if (!user.getPassword().equals(hashed))
            return Result.fail("用户名或密码错误");

        if (user.getStatus() == 0)
            return Result.fail("账号已禁用");

        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("deptParentId", "1");
        return Result.ok(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
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

    // ===== 角色管理 =====
    @PostMapping("/adminRole/getAllRoleList")
    public Result<List<Map<String, Object>>> getAllRoleList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(new ArrayList<>());
    }

    // ===== 菜单管理 =====
    @PostMapping("/adminMenu/queryHeaderMenuList")
    public Result<List<Map<String, Object>>> queryHeaderMenuList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(new ArrayList<>());
    }

    @PostMapping("/adminMenu/queryAllMenuList")
    public Result<List<Map<String, Object>>> queryAllMenuList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(new ArrayList<>());
    }

    @PostMapping("/adminConfig/setHeaderModelSort")
    public Result<Void> setHeaderModelSort(@RequestBody Map<String, Object> params) {
        return Result.ok();
    }
}
