package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-05 12:58
 */
@RestController("userDishController")
@Api("菜品相关接口")
@RequestMapping("/user/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     * 改造接口-实现缓存分类菜品
     *
     * redis放入的类型和取出的类型是一样的，不用担心类型问题
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        // 构造redis中的key，规则 dish_分类id
        String key = "dish_" + categoryId;
        // 查询redis中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        // 如果存在，直接返回缓存中的数据
        if (list != null && list.size() > 0) {
            return Result.success(list);
        }
        // 如果不存在，查询数据库，将查询到的数据存入redis中

        // 构建一个有分类id，和状态为起售的菜品
        Dish dish = Dish.builder().status(StatusConstant.ENABLE).categoryId(categoryId).build();

        list = dishService.listWithFlavor(dish);

        // 查询完之后，将数据写入redis
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

}
