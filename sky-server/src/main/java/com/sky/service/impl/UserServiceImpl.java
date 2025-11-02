package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatProperties weChatProperties;

    public User login(UserLoginDTO userLoginDTO) {
        // get the openid from weChat server
        String openid = getOpenId(userLoginDTO.getCode());

        // check if the user is a new user
        User user = userMapper.getByOpenId(openid);
        log.info("User login with openid: {}", openid);
        if (user == null) {
            // if not, create a new user
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        return user;
    }

    private String getOpenId(String code) {
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");
        // return json string
        String json = HttpClientUtil.doGet(WX_LOGIN, params);
        // using fastjson to parse json string to an object
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
