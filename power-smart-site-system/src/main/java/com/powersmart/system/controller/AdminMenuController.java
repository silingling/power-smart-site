package com.powersmart.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SysMenu;
import com.powersmart.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单/权限管理 — 同业电力前端 build/adminMenu/*
 *
 * 各端点命名与网关路由 /adminMenu/** 对齐。
 */
@RestController
@RequestMapping("/adminMenu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final SysMenuService menuService;

    /** 查询顶部一级菜单列表（用于角色分配时的菜单选择器） */
    @PostMapping("/queryHeaderMenuList")
    public Result<List<Map<String, Object>>> queryHeaderMenuList(@RequestBody(required = false) Map<String, Object> params) {
        List<SysMenu> headers = menuService.getHeaderMenus();
        List<Map<String, Object>> list = buildMenuTreeMap(headers);
        return Result.ok(list);
    }

    /** 查询完整菜单树 */
    @PostMapping("/queryAllMenuList")
    public Result<List<Map<String, Object>>> queryAllMenuList(@RequestBody(required = false) Map<String, Object> params) {
        List<SysMenu> tree = menuService.getMenuTree();
        List<Map<String, Object>> list = buildMenuTreeMap(tree);
        return Result.ok(list);
    }

    /** 创建菜单 */
    @PostMapping("/addMenu")
    public Result<Void> addMenu(@RequestBody Map<String, Object> params) {
        String name = params.getOrDefault("name", "").toString();
        if (StrUtil.isBlank(name)) return Result.fail("菜单名称不能为空");

        SysMenu menu = new SysMenu();
        menu.setParentId(params.containsKey("parentId")
                ? Long.parseLong(params.get("parentId").toString()) : 0L);
        menu.setName(name);
        menu.setPermissionKey(params.getOrDefault("permissionKey", "").toString());
        menu.setPath(params.getOrDefault("path", "").toString());
        menu.setIcon(params.getOrDefault("icon", "").toString());
        menu.setMenuType(params.containsKey("menuType")
                ? Integer.parseInt(params.get("menuType").toString()) : 2);
        menu.setVisible(params.containsKey("visible")
                ? Integer.parseInt(params.get("visible").toString()) : 1);
        menu.setSortOrder(params.containsKey("sortOrder")
                ? Integer.parseInt(params.get("sortOrder").toString()) : 0);
        menu.setStatus(1);
        menuService.save(menu);
        return Result.ok();
    }

    /** 更新菜单 */
    @PostMapping("/setMenu")
    public Result<Void> setMenu(@RequestBody Map<String, Object> params) {
        if (!params.containsKey("id") && !params.containsKey("menuId")) {
            return Result.fail("菜单 ID 不能为空");
        }
        Long id = Long.parseLong(
                params.getOrDefault("id", params.getOrDefault("menuId", 0)).toString());
        SysMenu menu = menuService.getById(id);
        if (menu == null) return Result.fail("菜单不存在");

        if (params.containsKey("name")) menu.setName(params.get("name").toString());
        if (params.containsKey("parentId")) menu.setParentId(Long.parseLong(params.get("parentId").toString()));
        if (params.containsKey("permissionKey")) menu.setPermissionKey(params.get("permissionKey").toString());
        if (params.containsKey("path")) menu.setPath(params.get("path").toString());
        if (params.containsKey("icon")) menu.setIcon(params.get("icon").toString());
        if (params.containsKey("menuType")) menu.setMenuType(Integer.parseInt(params.get("menuType").toString()));
        if (params.containsKey("visible")) menu.setVisible(Integer.parseInt(params.get("visible").toString()));
        if (params.containsKey("sortOrder")) menu.setSortOrder(Integer.parseInt(params.get("sortOrder").toString()));
        if (params.containsKey("status")) menu.setStatus(Integer.parseInt(params.get("status").toString()));
        menuService.updateById(menu);
        return Result.ok();
    }

    /** 删除菜单 */
    @PostMapping("/delMenu/{id}")
    public Result<Void> delMenu(@PathVariable Long id) {
        // 检查是否有子菜单
        long childCount = menuService.lambdaQuery().eq(SysMenu::getParentId, id).count();
        if (childCount > 0) return Result.fail("请先删除子菜单");
        menuService.removeById(id);
        return Result.ok();
    }

    /** 获取单个菜单详情 */
    @PostMapping("/getMenuInfo/{id}")
    public Result<Map<String, Object>> getMenuInfo(@PathVariable Long id) {
        SysMenu menu = menuService.getById(id);
        if (menu == null) return Result.fail("菜单不存在");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", menu.getId());
        m.put("parentId", menu.getParentId());
        m.put("name", menu.getName());
        m.put("permissionKey", menu.getPermissionKey());
        m.put("path", menu.getPath());
        m.put("icon", menu.getIcon());
        m.put("menuType", menu.getMenuType());
        m.put("visible", menu.getVisible());
        m.put("sortOrder", menu.getSortOrder());
        m.put("status", menu.getStatus());
        return Result.ok(m);
    }

    // ==================== 内部帮助方法 ====================

    /** 将 SysMenu 树转为前端使用的 Map 树 */
    private List<Map<String, Object>> buildMenuTreeMap(List<SysMenu> tree) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMenu menu : tree) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", menu.getId());
            node.put("parentId", menu.getParentId());
            node.put("name", menu.getName());
            node.put("permissionKey", menu.getPermissionKey() != null ? menu.getPermissionKey() : "");
            node.put("path", menu.getPath() != null ? menu.getPath() : "");
            node.put("icon", menu.getIcon() != null ? menu.getIcon() : "");
            node.put("menuType", menu.getMenuType());
            node.put("visible", menu.getVisible());
            node.put("sortOrder", menu.getSortOrder());
            node.put("status", menu.getStatus());

            if (CollUtil.isNotEmpty(menu.getChildren())) {
                node.put("children", buildMenuTreeMap(menu.getChildren()));
            }
            result.add(node);
        }
        return result;
    }
}
