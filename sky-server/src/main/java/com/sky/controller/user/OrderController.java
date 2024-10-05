package com.sky.controller.user;


import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation(value = "用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        OrderSubmitVO orderSubmitVO = orderService.orderSubmit(ordersSubmitDTO);


        return Result.success(orderSubmitVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation(value = "历史订单查询")
    public Result<PageResult> history(@RequestParam(value="page") int pageNum,@RequestParam int pageSize,@RequestParam(required = false) Integer status){
        //查询列表
        PageResult pageResult = orderService.pageQuery(pageNum, pageSize, status);

        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation(value = "查询订单详细")
    public Result<OrderVO> details(@PathVariable Long id){
        //查询订单详细
        OrderVO details = orderService.details(id);

        return Result.success(details);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation(value = "取消订单")
    public Result cancel(@PathVariable Long id){
        orderService.cancel(id);

        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation(value = "再来一单")
    public Result repetition(@PathVariable Long id){
        orderService.repetition(id);

        return Result.success();
    }

}
