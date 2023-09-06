package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-08-30 23:09
 */
@RestController
@Api(tags = "套餐管理")
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 套餐分页查询-但是套餐是没数据的，所以不出来内容
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("收到了分页请求,参数为: {}", setmealPageQueryDTO);
        // service返回一个pageResult对象
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 新增套餐的接口
     *
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    // 新增的套餐一定有对应的分类,所以根据分类id来删除这个缓存,精确删除
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐数据：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 根据id查询套餐以及套餐对应的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐以及套餐对应的菜品列表")
    public Result<SetmealVO> getWithSetmealDishesById(@PathVariable Long id) {
        log.info("根据id查询套餐以及套餐对应的菜品列表，参数为：{}", id);
        SetmealVO setmealVO = setmealService.getWithSetmealDishById(id);
        return Result.success(setmealVO);
    }

    @PostMapping("status/{status}")
    @ApiOperation("套餐起售/停售")
    // 由于不能直接拿到套餐的分类id,所以选择直接清空缓存
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改套餐")
    // 理由是更改的信息具有多样性,所以选择直接清空缓存
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改的参数:{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    // 由于不能直接拿到套餐的分类id,所以选择直接清空缓存
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result deleteBitch(@RequestParam List<Long> ids) {
        log.info("批量删除菜品,参数:{}", ids);
        setmealService.deleteBitch(ids);
        return Result.success();
    }

}
