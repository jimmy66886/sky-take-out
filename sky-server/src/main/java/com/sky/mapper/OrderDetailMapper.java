package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-08 9:16
 */
@Mapper
public interface OrderDetailMapper {
    void insert(List<OrderDetail> orderDetailList);
}
