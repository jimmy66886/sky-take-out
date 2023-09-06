package com.sky.controller.user;

import com.sky.context.BaseContextByMe;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author zzmr
 * @create 2023-09-06 20:41
 */
@RestController
@Api(tags = "购物车相关接口")
@Slf4j
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车:{}", shoppingCartDTO);
        shoppingCartService.addShoppingCard(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 这个接口不需要任何参数,因为用户的id可以直接取出
     *
     * @return
     */
    @ApiOperation("查询购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> list = shoppingCartService.getByUserId();
        return Result.success(list);
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result cleanCart() {
        return Result.success();
    }

}
