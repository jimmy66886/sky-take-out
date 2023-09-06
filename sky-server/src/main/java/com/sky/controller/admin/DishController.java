package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 *
 * @author zzmr
 * @create 2023-08-29 13:41
 */
@RestController
@Api(tags = "菜品相关接口")
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品");
        dishService.saveWithFlavor(dishDTO);

        // new
        // 清理缓存数据-清理对应类型的缓存数据，先确定这个菜品的分类id，然后清空该分类id的缓存
       cleanCache("dish_" + dishDTO.getCategoryId());

        return Result.success();
    }

    /**
     * 分页查询菜品
     *
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("分页查询菜品")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询开始");
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteBatchDish(@RequestParam List<Long> ids) {

        // 是可以拿到的,一个数组
        log.info("传入的ids: {}", ids.size());
        dishService.deleteBatchDish(ids);

        // new
        // 批量清空缓存
        /*for (Long id : ids) {
            // 获取每一个菜品的分类id,这里要用到 一个 根据菜品id查询菜品的信息的方法
            Dish dish = dishService.getById(id);
            String key = "dish_" + dish.getCategoryId();
            redisTemplate.delete(key);
        }*/

        // 那么还有一种方法,就是直接清空全部的缓存,所有以dish_开头的key--简单粗暴且有效
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 提取出清空缓存的方法
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

    /**
     * 菜品起售、停售
     *
     * @param status 新状态
     * @param id     菜品id
     * @return
     */
    @PostMapping("status/{status}")
    @ApiOperation("菜品的起售/停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {

        // 将要更改的状态和被更改菜品的id传入
        dishService.startOrStop(status, id);

        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据id查询菜品,和对应的口味
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品,和对应的口味")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     *
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品:{} ", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // new -清空缓存--但是修改时是可以修改菜品的分类的,如果菜品的分类修改了,那就要清空原本的和新的分类菜品的缓存了,也是直接全部清空?---直接清空全部
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getByCategoryId(Long categoryId) {
        log.info("根据分类id查询菜品，参数为{}", categoryId);
        List<Dish> list = dishService.getByCategoryId(categoryId);
        return Result.success(list);
    }

}
