package com.powersmart.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.system.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /** 查询角色关联的菜单 ID 列表 */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /** 批量删除角色的所有菜单关联 */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /** 查询多个角色关联的所有菜单 ID （去重） */
    @Select({
            "<script>",
            "SELECT DISTINCT menu_id FROM sys_role_menu WHERE role_id IN",
            "<foreach item='id' collection='roleIds' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
