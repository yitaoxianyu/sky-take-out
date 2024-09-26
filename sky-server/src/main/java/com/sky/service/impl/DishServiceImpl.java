package com.sky.service.impl;


import com.alibaba.druid.sql.parser.NotAllowCommentException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private FlavorMapper flavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //添加菜品
        dishMapper.insert(dish);

        Long id = dish.getId();
        //添加菜品的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
        }
        flavorMapper.insertBatch(flavors);

    }
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
            //首先判断id是否在售卖
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(Objects.equals(dish.getStatus(), StatusConstant.ENABLE))
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
            //判断选中的菜品id在套餐中
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(ids);
        if(setmealIds != null && !setmealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品
        for (Long id : ids) {
            dishMapper.deleteById(id);
            flavorMapper.deleteByDishId(id);
        }
    }

    public DishVO getByIdWithFlavors(Long id) {

        Dish dish = dishMapper.getById(id);

        //获取口味

        List<DishFlavor> flavors = flavorMapper.getByDishId(id);

        //构建VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    //修改时填充公共字段
    public void updateWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //先删除后插入
        flavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> flavors = flavorMapper.getByDishId(dishDTO.getId());

        flavors.forEach(dishFlavor -> {
            dishFlavor.setDishId(dishDTO.getId());
        });

        flavorMapper.insertBatch(flavors);

    }


    public List<Dish> list(String categoryId) {
        List<Dish> dishes = dishMapper.list(categoryId);
        return dishes;
    }


}
