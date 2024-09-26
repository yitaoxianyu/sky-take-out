package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DishService {

//结构中写函数不能使用大括号，编辑器会识别成实现函数。
    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);


    void deleteBatch(List<Long> ids);

    DishVO getByIdWithFlavors(Long id);

    void updateWithFlavors(DishDTO dishDTO);

    List<Dish> list(String categoryId);
}
