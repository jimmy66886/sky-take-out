package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-06 20:44
 */
public interface ShoppingCartService {
    void addShoppingCard(ShoppingCartDTO shoppingCartDTO);

    /**
     * 根据userId查询购物车
     *
     * @return
     */
    List<ShoppingCart> getByUserId();
}
