package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-08-29 21:58
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品Id查询对应的套餐Id
     * 这里是多对多
     * @param dishIds
     * @return
     * select setmeal_id from setmeal_dish where dish_id in (dishIds)
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

}
