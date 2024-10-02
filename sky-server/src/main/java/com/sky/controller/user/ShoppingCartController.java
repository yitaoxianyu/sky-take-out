package com.sky.controller.user;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "C端购物车相关接口")
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @RequestMapping("/add")
    @ApiOperation(value = "添加购物车")
    public Result add(@RequestBody  ShoppingCartDTO shoppingCartDTO){
        log.info("物品信息为:{}",shoppingCartDTO);

        shoppingCartService.add(shoppingCartDTO);

        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "查看购物车")
    public Result<List<ShoppingCart>> list(){
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        return Result.success(list);
    }

    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车")
    public Result clean(){
        shoppingCartService.clean();

        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation(value = "删除餐品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除物品为:{}",shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);

        return Result.success();
    }

}
