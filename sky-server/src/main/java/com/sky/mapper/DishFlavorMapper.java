package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
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

    /**
     * 根据dish_id删除
     *
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId};")
    void deleteByDishId(Long dishId);
}
