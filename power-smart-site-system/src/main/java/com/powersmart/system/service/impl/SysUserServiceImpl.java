package com.powersmart.system.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.exception.BusinessException;
import com.powersmart.system.entity.SysUser;
import com.powersmart.system.mapper.SysUserMapper;
import com.powersmart.system.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser login(String username, String password) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) throw new BusinessException("用户不存在");

        // 密码校验：md5(raw_password) compare with stored hash
        String hashed = DigestUtil.md5Hex(password);
        if (!user.getPassword().equals(hashed)) throw new BusinessException("密码错误");
        return user;
    }
}
