package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    // Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     *
     * @param setmeal
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    @Select("select * from setmeal where id = #{id};")
    Setmeal getById(Long id);

    /**
     * 根据id修改
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据批量id删除套餐
     */
    void deleteBitch(List<Long> ids);

    /**
     * 通过关联查询进行分页
     *
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQueryBySql(SetmealPageQueryDTO setmealPageQueryDTO);


    List<Setmeal> getByCategoryId(Setmeal setmeal);

    @Select("SELECT setmeal_dish.copies,dish.description,dish.image,dish.`name`" +
            "FROM setmeal_dish LEFT JOIN dish ON setmeal_dish.dish_id = dish.id WHERE setmeal_id = #{id} ")
    List<DishItemVO> getDishItemById(Long id);

    /**
     * 根据状态查询套餐数量
     *
     * @param status
     * @return
     */
    @Select("select count(id) from setmeal where status = #{status}")
    Integer getCountByStatus(Integer status);
}
