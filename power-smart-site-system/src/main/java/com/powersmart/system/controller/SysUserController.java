package com.powersmart.system.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SysUser;
import com.powersmart.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @PostMapping("/users")
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        userService.save(user);
        return Result.ok(user);
    }

    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(required = false) Long projectId) {
        return Result.ok(userService.lambdaQuery()
                .eq(projectId != null, SysUser::getProjectId, projectId)
                .list());
    }

    @PostMapping("/login")
    public Result<SysUser> login(@RequestParam String username, @RequestParam String password) {
        return Result.ok(userService.login(username, password));
    }
}
