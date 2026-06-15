package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SysDept;
import com.powersmart.system.entity.SysUser;
import com.powersmart.system.mapper.SysDeptMapper;
import com.powersmart.system.mapper.SysUserMapper;
import com.powersmart.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统管理 — 同业电力前端 build/adminUser/* — 登录/用户/部门/业务类型
 */
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final SysUserService userService;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    // ==================== 登录 ====================

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password))
            return Result.fail("用户名或密码不能为空");

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null || !user.getPassword().equals(password))
            return Result.fail("用户名或密码错误");
        if (user.getStatus() == 0)
            return Result.fail("账号已禁用");

        // 生成简单 token（实际应替换为 JWT）
        String token = UUID.randomUUID().toString().replace("-", "");

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
        // 初始化用户信息（返回当前用户）
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("userId", 1);
        info.put("username", "admin");
        info.put("realName", "管理员");
        info.put("deptParentId", "1");
        return Result.ok(info);
    }

    @PostMapping("/adminUser/queryUserList")
    public Result<PageResult<Map<String, Object>>> queryUserList(@RequestBody(required = false) Map<String, Object> params) {
        Page<SysUser> page = new Page<>(1, 50);
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
            list.add(m);
        }
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
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
}
