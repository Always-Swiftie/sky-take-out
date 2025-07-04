package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import io.swagger.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO);
        //检验openid是否为空，是否登录失败
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //合法的微信用户,判断是否是一个“新用户”-openid在user表中是否存在
        User user = userMapper.getByOpenId(openid);
        //如果是新用户，需要在user表中完成注册，插入记录
        if(user == null){
          user = User.builder()
                  .openid(openid)
                  .createTime(LocalDateTime.now())
                  .build();
          userMapper.insert(user);
        }
        //最后返回user对象
        return user;
    }

    private String getOpenid(UserLoginDTO userLoginDTO) {
        Map<String,String> requestParam = new HashMap<>();
        requestParam.put("appid",weChatProperties.getAppid());
        requestParam.put("secret",weChatProperties.getSecret());
        requestParam.put("js_code", userLoginDTO.getCode());
        requestParam.put("grant_type","authorization_code");
        //调用微信接口，获取用户的openid
        String json = HttpClientUtil.doGet(WX_LOGIN,requestParam);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
