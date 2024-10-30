package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 微信登录
     * @param code
     * @return
     */
    @Override
    public User login(String code) {
        // 获取openId
        String openId = getOpenId(code);
        // 判断openId是否为空，如果为空，抛出业务异常
        if (openId == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 根据openId查询用户对象
        User user = userMapper.getByOpenId(openId);
        // 判断当前用户是否为新用户，如果是新用户，则自动完成注册
        if (user == null) {
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        // 返回用户对象
        return user;
    }


    /**
     * 获取openId
     * @param code
     * @return
     */
    private String getOpenId(String code) {
        HashMap<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        // 发送请求,返回的数据封装为json格式
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        // 获取openId
        JSONObject jsonObject = JSON.parseObject(json);
        String openId = jsonObject.getString("openid");

        // 返回openId
        return openId;
    }
}
