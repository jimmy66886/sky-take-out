package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

/**
 * @author zzmr
 * @create 2023-09-13 15:14
 */
public interface WorkSpaceService {
    /**
     * 查询查询今日运营数据
     *
     * @return
     */
    BusinessDataVO businessData();

    /**
     * 查询套餐总览
     *
     * @return
     */
    SetmealOverViewVO overviewSetmeals();

    /**
     * 查询菜品总览
     *
     * @return
     */
    DishOverViewVO overviewDishes();

    /**
     * 订单总览
     *
     * @return
     */
    OrderOverViewVO overviewOrder();
}
