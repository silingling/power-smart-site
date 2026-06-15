package com.powersmart.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powersmart.system.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /** 获取完整菜单树 */
    List<SysMenu> getMenuTree();

    /** 将平面列表构建为树结构 */
    List<SysMenu> buildTree(List<SysMenu> flatList);

    /** 获取可见的顶部菜单（parent_id=0，menu_type=1）*/
    List<SysMenu> getHeaderMenus();
}
