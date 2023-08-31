package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
     *
     * @param dishIds
     * @return select setmeal_id from setmeal_dish where dish_id in (dishIds)
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 插入套餐-菜品信息
     * 这个没有这几个公共字段，不能添加这个注解
     *
     * @param setmealDishes
     */
    // @AutoFill(value = OperationType.INSERT)
    void insert(List<SetmealDish> setmealDishes);

    /**
     * 根据id在 setmeal_dish表中查询关联的信息
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id};")
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据setmealId来删除套餐与菜品的对应关系
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
