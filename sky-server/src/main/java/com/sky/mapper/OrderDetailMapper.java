package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-08 9:16
 */
@Mapper
public interface OrderDetailMapper {
    void insert(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id查询订单详情,就是查询出这个订单的所有菜品/套餐
     *
     * @param ordersId
     * @return
     */
    @Select("select * from order_detail where order_id = #{ordersId}")
    List<OrderDetail> getByOrderId(Long ordersId);
}
