package com.sky.service;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.core.annotation.Order;

import java.util.List;

public interface OrderService {

    OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO);

    PageResult pageQuery(int pageNum,int pageSize,Integer status);

    OrderVO details(Long id);

    void cancel(Long id);

    void repetition(Long id);
}
