package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders order);

    Page<Orders> pageQuery(OrdersPageQueryDTO pageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Delete("delete from orders where id = #{id}")
    void deleteById(Long id);

    void update(Orders orders);

    Integer countStatus(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndTimeTL(Integer status, LocalDateTime time);

    @Select("select * from orders where number = #{orderNumber} and user_id = #{userId}")
    Orders getByNumberAndUserId(String orderNumber,Long userId);

    Double sumByMap(Map map);

    Integer countByMap(Map map);
}
