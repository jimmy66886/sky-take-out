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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzmr
 * @create 2023-09-05 8:54
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登陆
     * <p>
     * 如果是新用户，就会自动完成注册，封装user然后返回
     * 如果不是，则直接在数据库中就查出了该user对象，也是返回
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 差不多就是，先拿着code和用户id进行查询，然后封装到一个user里，最后返回

        String openId = getString(userLoginDTO);

        // 1.1 判断openId是否为空，如果为空，则抛出异常
        if (openId == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 2. 判断当前用户是否为新用户-根据openId
        User user = userMapper.getByOpenId(openId);
        // 2.1 是新用户，则自动完成注册
        if (user == null) {
            // 是新的用户 -- 构造用户信息-完成注册 -- 现在只能拿到openId和创建时间，其余的是拿不到的
            user = User.builder().openid(openId).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        // 2.2 返回用户对象
        return user;
    }

    /**
     * 将根据userLoginDTO获取openId的片段抽取成一个方法
     *
     * @param userLoginDTO
     * @return
     */
    private String getString(UserLoginDTO userLoginDTO) {
        // 1. 调用微信接口服务 获取openId
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appId", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", userLoginDTO.getCode());
        paramMap.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, paramMap);

        JSONObject jsonObject = JSON.parseObject(json);
        String openId = jsonObject.getString("openid");
        return openId;
    }
}
