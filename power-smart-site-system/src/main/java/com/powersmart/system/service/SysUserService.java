package com.powersmart.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powersmart.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser login(String username, String password);
}
