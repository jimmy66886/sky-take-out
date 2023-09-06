package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzmr
 * @create 2023-08-29 13:44
 */

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        /**
         * 数据库直接查到的是Dish,而前端要求的是DishVO,因为DishVO中才有分类名称的,那怎么查呢?
         * 原来是用到了多表联查,只不过返回结果是VO罢了,只需要改一下Sql,改一下返回结果泛型就OK了
         */
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        log.info("查询到的分页信息:{}", page);
        long total = page.getTotal();
        List<DishVO> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Override
    public void deleteBatchDish(List<Long> ids) {

        // 1. 判断当前菜品是否能够删除-是否存在起售中
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                // 当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 2. 是否在某个套餐中
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            // 查到了对应的套餐-该菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        for (Long id : ids) {
            // 3. 删除菜品数据，还有菜品关联的口味数据
            dishMapper.deleteById(id);
            // 删除口味相关--有就删除，没有就算了，不用查
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    /**
     * 菜品的起售/停售
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 直接通过builder来构建一个对象
        Dish dish = Dish.builder().status(status).id(id).build();
        // 然后调用之前写的更新方法即可
        dishMapper.update(dish);
    }

    /**
     * 根据id查询菜品,和对应的口味
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {

        // 根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 根据菜品id查询口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        // 将查询到的口味数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     * 学到新东西了，如果**一个东西直接修改很难办，可以考虑先删除再添加**
     *
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        // 先将dishDTO中的基本信息存入数据库
        Dish dish = new Dish();
        // 还是使用的老办法，拷贝对象的属性
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        // 再将更新后的口味信息存入数据库，这里要用到dishFlavorMapper
        // 但是由于口味修改很难确定，是多了是少了，还是没改，所以我们可以选择，先将原先的口味删除，再添加新的口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }


    }

    /**
     * 根据分类id查询菜品
     * 对比答案,发现问题了,这里查的是所有菜品,不管状态如何
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        // 之前写的
        // List<Dish> list = dishMapper.getByCategoryId(categoryId);
        // return list;

        // 修改后
        Dish dish = Dish.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        return dishMapper.getByCategoryId(dish);

    }

    /**
     * 根据dish的分类id进行查询
     *
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {

        // 先根据dish中的分类id进行查询，查到对应分类所有的菜品
        List<Dish> dishList = dishMapper.getByCategoryId(dish);
        List<DishVO> dishVOList = new ArrayList<>();

        // 遍历这个dishList，拿到每一个dish,然后将每一个dish的口味都查出来，封装到一个dishVO中，再添加到dishVOList中
        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

    @Override
    public Dish getById(Long id) {
        Dish dish = dishMapper.getById(id);
        return dish;
    }
}
