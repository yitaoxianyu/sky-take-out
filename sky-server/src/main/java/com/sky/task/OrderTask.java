package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        //查询在当前时间前15分钟的订单，然后将其状态改为未支付
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        log.info("当前时间:{}",time);

        List<Orders> ordersList = orderMapper.getByStatusAndTimeTL(Orders.PENDING_PAYMENT, time);

        if(ordersList != null && !ordersList.isEmpty()){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时");
                orders.setCancelTime(LocalDateTime.now());

                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        log.info("当前时间:{}",time);

        List<Orders> ordersList = orderMapper.getByStatusAndTimeTL(Orders.DELIVERY_IN_PROGRESS, time);
        if(ordersList != null && !ordersList.isEmpty()){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);

                orderMapper.update(orders);
            }
        }


    }


}
