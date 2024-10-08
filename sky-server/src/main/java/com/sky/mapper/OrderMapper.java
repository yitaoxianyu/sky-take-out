package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}
