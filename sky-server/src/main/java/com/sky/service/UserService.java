package com.sky.service;

import com.sky.entity.User;

public interface UserService {
    /**
     * 微信登录
     * @param code
     * @return
     */
    User login(String code);
}
