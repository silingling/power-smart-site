package com.powersmart.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.exception.BusinessException;
import com.powersmart.system.entity.SysUser;
import com.powersmart.system.mapper.SysUserMapper;
import com.powersmart.system.service.SysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    /**
     * 首次启动时检查：若 admin 密码还是 MD5 旧哈希，自动升级为 BCrypt
     */
    public SysUserServiceImpl() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public SysUser login(String username, String password) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) throw new BusinessException("用户不存在");

        // 兼容旧 MD5（升级过渡期）
        if (isMd5Hash(user.getPassword())) {
            String md5Hex = cn.hutool.crypto.digest.DigestUtil.md5Hex(password);
            if (!user.getPassword().equals(md5Hex)) throw new BusinessException("密码错误");
            // 自动升级为 BCrypt
            user.setPassword(passwordEncoder.encode(password));
            updateById(user);
            return user;
        }

        // BCrypt 校验
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return user;
    }

    /**
     * 注册/创建用户（BCrypt 加密）
     */
    public SysUser createUser(String username, String rawPassword, String realName, String phone) {
        if (getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) != null) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setStatus(1);
        save(user);
        return user;
    }

    /**
     * 判断是否为 MD5 旧哈希（32 位十六进制）
     */
    private boolean isMd5Hash(String hash) {
        return hash != null && hash.length() == 32 && hash.matches("[a-f0-9]{32}");
    }
}
