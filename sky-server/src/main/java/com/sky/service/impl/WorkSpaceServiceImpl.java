package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzmr
 * @create 2023-09-13 15:14
 */
@Service
@Slf4j
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 查询查询今日运营数据
     *
     * @return
     */
    @Override
    public BusinessDataVO businessData() {

        /**
         * 今日的数据，也就是当前日期的数据
         * 这样就拿到了今天的时间区间
         */
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        /**
         * 需要的数据
         * newUsers 新增用户数
         * orderCompletionRate 订单完成率
         * turnover 营业额
         * unitPrice 平均客单价
         * validOrderCount 有效订单数
         */

        // 1. newUsers
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        Integer newUser = userMapper.countByMap(map); // 可以直接用之前写的动态sql

        // 2. orderCompletionRate
        Integer orderCount = orderMapper.countOrderByMap(map);
        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countOrderByMap(map);
        Double orderCompletionRate = 0.0;
        if (orderCount != 0) {
            // 不是零，再除
            orderCompletionRate = validOrderCount / orderCount.doubleValue();
        }

        // 3. turnover 当天所有订单的总额 还是用之前的mapper
        Double turnover = orderMapper.sumByMap(map);
        // 没考虑到这个问题，当turnover为0时要赋值0.0，不然是空的
        turnover = turnover == null ? 0.0 : turnover;

        // 4. unitPrice  平均客单价 要有当天下单的用户量，然后拿上面的总额一除就完了
        Integer totalOrderUser = orderMapper.getDistinctUser(map);
        Double unitPrice = 0.0;
        if (totalOrderUser != 0) {
            unitPrice = turnover / totalOrderUser.doubleValue();
        }

        return BusinessDataVO.builder()
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .newUsers(newUser)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    @Override
    public SetmealOverViewVO overviewSetmeals() {

        /**
         * 套餐总览 1. 已停售套餐数量  2. 已启售套餐数量
         */
        // 启售
        Integer startCount = setmealMapper.getCountByStatus(StatusConstant.ENABLE);
        Integer stopCount = setmealMapper.getCountByStatus(StatusConstant.DISABLE);

        return SetmealOverViewVO.builder().sold(startCount).discontinued(stopCount).build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {

        Integer startCount = dishMapper.getCountByStatus(StatusConstant.ENABLE);
        Integer stopCount = dishMapper.getCountByStatus(StatusConstant.DISABLE);

        return DishOverViewVO.builder().sold(startCount).discontinued(stopCount).build();
    }

    /**
     * 订单总览
     *
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrder() {

        Map map = new HashMap();
        Integer allOrders = orderMapper.countOrderByMap(map);
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.countOrderByMap(map);
        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countOrderByMap(map);
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.countOrderByMap(map);
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countOrderByMap(map);

        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .build();
    }
}
