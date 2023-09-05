package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author zzmr
 * @create 2023-09-05 9:16
 */
@Mapper
public interface UserMapper {

    /**
     * 根据openId查询用户
     *
     * @param openId
     * @return
     */
    @Select("select * from user where openid = #{openId}")
    User getByOpenId(String openId);


    /**
     * 用户注册
     * @param user
     */
    void insert(User user);
}
