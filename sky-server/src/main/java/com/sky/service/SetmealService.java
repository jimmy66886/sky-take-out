package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-08-30 23:15
 */
public interface SetmealService {


    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐以及套餐对应的菜品列表
     * @param id
     * @return
     */
    SetmealVO getWithSetmealDishById(Long id);

    /**
     * 套餐起售/停售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改套餐
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 根据批量id删除套餐
     * @param ids
     */
    void deleteBitch(List<Long> ids);
}
