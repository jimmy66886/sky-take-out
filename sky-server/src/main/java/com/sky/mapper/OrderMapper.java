package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author zzmr
 * @create 2023-09-08 9:16
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单支付状态设置为已支付，并且订单状态设置为待接单
     *
     * @param orderNumber
     */
    @Update("update orders set pay_status = 1,status = 2 where number = #{orderNumber}")
    void changePayStatus(String orderNumber);

    // 查询订单列表
    Page<Orders> list(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 获取所有的订单
     *
     * @return
     */
    @Select("select * from orders")
    List<Orders> getAllOrders();

    /**
     * 根据状态查询数量
     *
     * @param toBeConfirmed
     * @return
     */
    @Select("select count(id) from orders where status = #{status} ")
    Integer countStatus(Integer toBeConfirmed);

    @Update("update orders set pay_status = 1,status = 2,checkout_time = #{checkoutTime} where number = #{orderNumber}")
    void changeStatusAndCheckoutTime(String orderNumber, LocalDateTime checkoutTime);

    /**
     * 根据订单状态和下单时间查询
     *
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    Double sumByMap(Map map);

    Integer countOrderByMap(Map map);

}
