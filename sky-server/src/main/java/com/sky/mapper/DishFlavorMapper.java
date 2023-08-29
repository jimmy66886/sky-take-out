package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-08-29 14:20
 */
@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     *
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);
}
