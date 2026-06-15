package com.powersmart.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.system.entity.SysMenu;
import com.powersmart.system.mapper.SysMenuMapper;
import com.powersmart.system.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> all = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .eq(SysMenu::getIsDeleted, 0)
                .orderByAsc(SysMenu::getSortOrder));
        return buildTree(all);
    }

    @Override
    public List<SysMenu> buildTree(List<SysMenu> flatList) {
        if (flatList == null || flatList.isEmpty()) return Collections.emptyList();

        // 按 parentId 分组
        Map<Long, List<SysMenu>> childrenMap = flatList.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() > 0)
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        // 为每个节点挂载子节点
        for (SysMenu menu : flatList) {
            List<SysMenu> children = childrenMap.get(menu.getId());
            if (children != null) {
                children.sort(Comparator.comparingInt(SysMenu::getSortOrder));
                menu.setChildren(children);
            }
        }

        // 返回顶级节点
        return flatList.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<SysMenu> getHeaderMenus() {
        return list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, 0)
                .eq(SysMenu::getStatus, 1)
                .eq(SysMenu::getIsDeleted, 0)
                .eq(SysMenu::getVisible, 1)
                .orderByAsc(SysMenu::getSortOrder));
    }
}
