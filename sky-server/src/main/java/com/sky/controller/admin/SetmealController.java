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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation(value = "新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        setmealService.saveWithDishes(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询:{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }



    @DeleteMapping
    @ApiOperation(value = "删除套餐")
//    参数类型转换
    public Result deleteBatch(@RequestParam List<Long> ids){
        setmealService.deleteBatch(ids);

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value = "套餐起售/停售")
    public Result status(@PathVariable Integer status,@RequestParam Integer id){
        setmealService.updateStatus(status,id);

        return Result.success();
    }


    @GetMapping("{id}")
    @ApiOperation(value = "用于套餐回显")
    public Result<SetmealVO> getById(@PathVariable Long id){
        SetmealVO setmealVO = setmealService.getSetmealWithDish(id);

        return Result.success(setmealVO);
    }





    @PutMapping
    @ApiOperation(value = "更新套餐")
    //应该进行回显
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);
        return Result.success();
    }
}