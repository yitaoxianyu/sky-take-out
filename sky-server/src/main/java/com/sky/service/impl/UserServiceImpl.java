package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    WeChatProperties weChatProperties;

    @Autowired
    UserMapper userMapper;


    public static final String WX_LOGIN = "https://api.weixin.com/sns/jscode2session";

    public User wxLogin(UserLoginDTO userLoginDTO) {
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",userLoginDTO.getCode());
        map.put("grant_type","authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        JSONObject jsonObject = JSONObject.parseObject(json);
        String openid = jsonObject.getString("openid");
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        User user = userMapper.getByOpenid(openid);

        if(user == null){
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);

        }

        return user;
    }
}
