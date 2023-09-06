package com.sky.service.impl;

import com.sky.context.BaseContextByMe;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.DishService;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-06 20:44
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCard(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入到购物车中的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContextByMe.getCurrentId();
        shoppingCart.setUserId(userId);

        // 虽然返回的是一个集合,但是按照以上的条件,返回的结果应该只有一条
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        // 如果已经存在,则只需要数量加1
        if (list != null && list.size() > 0) {
            // 获取该条记录,然后
            ShoppingCart cart = list.get(0);
            // 数量加一
            cart.setNumber(cart.getNumber() + 1); // 执行sql update shopping_cart set number = ? where id = ?
            shoppingCartMapper.updateNumberById(cart);
        } else {
            // 如果不存在,需要插入一条购物车数据
            // 那就要构造这个购物车数据,只有前端传来的setmeal_id/dish_id 和dishFlavor 以及获取的userId,是不够的,还需要name,image
            // 先判断是菜品还是套餐
            Long dishId = shoppingCart.getDishId();


            if (dishId != null) {
                // 为菜品,先根据dishId查询该菜品信息,需要name和image
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                // 默认的数量
            } else {
                // 为套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
                // 默认的数量
            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            // 不管是插入哪一个,到这里时数据就已经封装好了
            shoppingCartMapper.insert(shoppingCart);
        }

    }
}
