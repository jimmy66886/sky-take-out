package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-06 21:08
 */
@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态sql
     * 根据shoppingCart中封装的数据,动态查询购物车表,然后返回的也是shoppingCart
     * 这样设计是由于添加购物车时的类型是不确定的,可能是套餐,也可能是菜品,可能是新加,也可能是之前有了
     *
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id修改数量
     *
     * @param cart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart cart);

    /**
     * 插入一条购物车数据
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "values (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据userId查询购物车
     * @param userId
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> getByUserId(Long userId);
}
