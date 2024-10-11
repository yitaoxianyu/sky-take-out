package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Api(tags = "统计数据相关接口")
@Slf4j
@RestController
@RequestMapping("/admin/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation(value = "统计营业额")
    public Result<TurnoverReportVO> turnoverReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        return Result.success(reportService.getTurnoverStatistics(begin,end));
    }

    @GetMapping("/userStatistics")
    @ApiOperation(value = "统计用户数量")
    public Result<UserReportVO> userReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end)
    {
        return Result.success(reportService.getUserStatistics(begin,end));
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation(value = "统计订单数量")
    public Result<OrderReportVO> orderReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end)
    {
        return Result.success(reportService.getOrderStatistics(begin,end));
    }
}
