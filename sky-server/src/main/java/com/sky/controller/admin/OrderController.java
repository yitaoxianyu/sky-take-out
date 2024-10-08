package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Slf4j
@Api(tags = "后台订单相关接口")
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PutMapping("/rejection")
    @ApiOperation(value = "拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {

        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @PostMapping("/cancel")
    @ApiOperation(value = "取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {

        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation(value = "完成订单")
    public Result complete(@PathVariable Long id){

        orderService.complete(id);
        return Result.success();
    }

    @GetMapping("/conditionSearch")
    @ApiOperation(value = "订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){

        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }


    @PutMapping("/delivery/{id}")
    @ApiOperation(value = "派送订单")
    public Result delivery(@PathVariable Long id){

        orderService.delivery(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    @ApiOperation(value = "各个状态订单的数量")
    public Result<OrderStatisticsVO> statistics(){

        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

}
