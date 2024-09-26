package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper
{

    List<Long> getSetmealIdsByDishId(List<Long> ids);

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBatch(List<Long> setmealIds);


    @Delete("delete from sky_take_out.setmeal_dish where setmeal_id = #{id}")
    void deleteBySetmealId(Long id);

    List<SetmealDish> getDishesBySetmealId(Long setmealId);
}

