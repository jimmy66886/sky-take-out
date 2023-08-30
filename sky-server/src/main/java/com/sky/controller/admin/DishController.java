package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品");
        dishService.saveWithFlavor(dishDTO);
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
        return Result.success();
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
        return Result.success();
    }

}
