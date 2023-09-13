package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);


    /*    @Insert("insert into dish(name,category_id,price,image,description,status,create_time,update_time,
    create_user," +
                "update_user) " +
                "values (#{name},#{categoryId},#{price},#{image},#{description},#{status},#{createTime},
                #{updateTime}," +
                "#{createUser},#{updateUser})")*/

    /**
     * 插入菜品数据
     *
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * 返回结果泛型是DishVO
     *
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品-根据主键删除
     *
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 修改菜品-又忘了设置自动填充了
     *
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品-但是要查询状态为1的,也就是启用的菜品
     *
     * @param dish
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId} and status = #{status}")
    List<Dish> getByCategoryId(Dish dish);

    /**
     * 根据套餐id查询菜品
     *
     * @param
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 根据状态查询数量
     *
     * @param enable
     * @return
     */
    @Select("SELECT count(id) FROM dish WHERE `status` = #{status}")
    Integer getCountByStatus(Integer enable);
}
