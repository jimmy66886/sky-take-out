package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-08-29 13:44
 */

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 向菜品表插入1条数据
        // 前端传来的dishDTO,中包含口味信息,但是菜品表中是没有口味信息的,所以还是要把这个DTO转换成Dish来传入数据库
        Dish dish = new Dish();
        // 将dto中需要的数据拷贝给dish
        BeanUtils.copyProperties(dishDTO, dish);
        // 终于给idea配置好了数据库,可以实现字段提示了
        dishMapper.insert(dish);

        // 获取insert语句生成的主键值
        Long dishId = dish.getId();

        // 先将dto中存放口味数据的集合取出
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            // 向口味表插入n条数据 - 因为1个菜品对应多个口味
            // 两种方法,可以遍历集合,然后插入,也可以批量插入
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }


    }
}
