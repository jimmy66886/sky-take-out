package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author zzmr
 * @create 2023-09-08 9:16
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单支付状态设置为已支付，并且订单状态设置为待接单
     * @param orderNumber
     */
    @Update("update orders set pay_status = 1,status = 2 where number = #{orderNumber}")
    void changePayStatus(String orderNumber);
}
