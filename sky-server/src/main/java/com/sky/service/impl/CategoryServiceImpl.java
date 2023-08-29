package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.context.BaseContextByMe;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        // 新增的菜单默认是禁止的
        category.setStatus(StatusConstant.DISABLE);

        // 然后是创建时间，修改时间，创建人，修改人
/*        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContextByMe.getCurrentId());
        category.setUpdateUser(BaseContextByMe.getCurrentId());*/

        categoryMapper.insert(category);
    }

    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {

        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

        long total = page.getTotal();
        List<Category> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 根据id删除分类
     * 但是要注意该分类中是否关联了菜品，如果关联了就不能删除
     * 还要注意是否关联了套餐
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {

        Integer count = dishMapper.countByCategoryId(id);

        if (count > 0) {
            // 该分类中有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            // 该分类中有套餐，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        // 没问题了就删除该分类数据
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类信息
     *
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

/*        category.setUpdateUser(BaseContextByMe.getCurrentId());
        category.setUpdateTime(LocalDateTime.now());*/

        categoryMapper.update(category);

    }

    /**
     * 禁用/启用 分类状态
     *
     * @param status
     * @param id
     */
    @Override
    @AutoFill(OperationType.UPDATE)
    public void startOrStop(Integer status, Long id) {
        Category category =
                Category.builder().id(id).status(status)
                        .updateTime(LocalDateTime.now()).updateUser(BaseContextByMe.getCurrentId()).
                        build();
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        List<Category> list = categoryMapper.list(type);
        return list;
    }
}
