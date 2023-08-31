package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-08-30 23:15
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
/*    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        // List<SetmealVO> result = page.getResult();

        */

    /**
     * 出现严重bug,下面的获取类型名是通过分页的第一项的类型名的,比如第一个是商务套餐,那么后面的就全是商务套餐,那肯定不行
     *//*

     *//*        if (result.size() > 0) {
            // 临时变量
            SetmealVO setmealVOD = result.get(0);
            String categoryName = categoryMapper.getById(setmealVOD.getCategoryId());
            page.forEach(setmealVO -> setmealVO.setCategoryName(categoryName));
        }*//*

        // 改成这样应该就行了,是遍历整个页面的所有项,分别获取每一项的类型名
        for (SetmealVO setmealVO : page) {
            String categoryName = categoryMapper.getById(setmealVO.getCategoryId());
            setmealVO.setCategoryName(categoryName);
            // 我明明记得给这个VO加入了这个对应的套餐列表,可就是没有?
            setmealVO.setSetmealDishes(setmealDishMapper.getBySetmealId(setmealVO.getId()));
        }

        // 根据分类id查询分类名，然后将分类名置入这个套餐VO中
        return new PageResult(page.getTotal(), page.getResult());
    }*/


    // 新写的一个Service 这个新的和之前写的区别在于,之前的分类名是后来查了存进去的,新的这个是通过关联查询出来的
    // 对啊,上面把套餐对应的菜品也放进去了,但是前端展示分页,那个页面就用不到套餐对应的菜品信息,只是修改时会用到
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQueryBySql(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 新增套餐
     *
     * @param setmealDTO 前端传来的带有套餐内菜品的套餐数据
     */
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {

        // 先讲setmealDTO转换成setmeal，然后执行一次插入
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        // 插入该套餐信息后，由于套餐和菜品是根据id绑定的，也就是说，在插入这条数据后，要获取到该条记录的id值
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        /**
         * 批量插入套餐-菜品，前端发来的数据中肯定是有dish_id的，所以要想插入，应该先把前面获取的setmealId赋值给setmealDishes的每一项
         */
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
        setmealDishMapper.insert(setmealDishes);

    }

    /**
     * 根据id查询套餐以及套餐对应的菜品列表
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getWithSetmealDishById(Long id) {
        // 第一步，先根据id查询套餐表
        Setmeal setmeal = setmealMapper.getById(id);
        log.info("获取到的套餐信息：{}", setmeal);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);

        // 第二步，根据id在 setmeal_dish表中查询关联的信息
        List<SetmealDish> list = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(list);

        // 别忘了还有分类名 通过分类id获取类型名----后加,好像用不到分类名
/*        String categoryName = categoryMapper.getById(setmeal.getCategoryId());
        setmealVO.setCategoryName(categoryName);*/

        return setmealVO;
    }

    /**
     * 套餐起售/停售
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {

        /**
         * 第一种是我写的，第二种是老师写的，两种方式，都能实现效果
         */

        if (status == StatusConstant.ENABLE){
            // 根据套餐id查询套餐关联的菜品id
            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            for (SetmealDish setmealDish : setmealDishes) {
                Dish dish = dishMapper.getById(setmealDish.getDishId());
                if (dish.getStatus() == StatusConstant.DISABLE) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }

        // 起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
/*        if (status == StatusConstant.ENABLE) {
            // select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && dishList.size() > 0) {
                dishList.forEach(dish -> {
                    if (StatusConstant.DISABLE == dish.getStatus()) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }*/


        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();


        // 写一个可以修改任意的字段
        setmealMapper.update(setmeal);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 还是要分成两步修改啊
        // 1. 先直接修改套餐的基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);


        // 2. 修改套餐对应的菜品信息,或者说是setmeal_dish表中的信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 2.1 先删除-根据setmealId删除
        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);
        // 2.2 再赋值
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        log.info("修改后的套餐菜品对象为:{}", setmealDishes);
        setmealDishMapper.insert(setmealDishes);
    }

    /**
     * 根据批量id删除套餐
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBitch(List<Long> ids) {
        // 1. 根据ids查询套餐
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                // 起售中,不能删除,抛出异常
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        // 好又发现问题了,这里只删除了

        // 2. 都是停售的,进行批量删除
        setmealMapper.deleteBitch(ids);
        for (Long id : ids) {
            setmealDishMapper.deleteBySetmealId(id);
        }
    }
}
