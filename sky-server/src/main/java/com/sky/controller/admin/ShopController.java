package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;


@RestController("adminShopController")
@Slf4j
@Api(tags = "商店相关接口")
@RequestMapping("/admin/shop")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String KEY = "SHOP_STATUS";

    @ApiOperation(value = "商家修改店铺状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("修改营业状态为{}",status == 1 ? "营业":"打烊");

        redisTemplate.opsForValue().set(KEY,status);

        return Result.success();
    }

    @ApiOperation(value = "商家获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);

        log.info("当前营业状态为{}",status == 1 ? "营业":"打烊");
        
        return Result.success(status);
    }

}
