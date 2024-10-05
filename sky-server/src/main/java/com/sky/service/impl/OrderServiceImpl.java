package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;


    public OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null) throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list == null || list.isEmpty()) throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());

        //插入数据
        orderMapper.insert(order);

        //把购物车中的商品插入到订单明细表中
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail od = new OrderDetail();
            BeanUtils.copyProperties(cart,od);
            od.setOrderId(order.getId());
            orderDetails.add(od);
        }
        orderDetailMapper.insertBatch(orderDetails);

        //清空购物车中的商品
        shoppingCartMapper.deleteByUserId(userId);


        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(order.getId())
                .orderNumber(order.getNumber())
                .orderTime(order.getOrderTime())
                .orderAmount(order.getAmount()).build();

        return orderSubmitVO;
    }



    public PageResult pageQuery(int pageNum,int pageSize,Integer status) {

        PageHelper.startPage(pageNum,pageSize);

        OrdersPageQueryDTO pageQueryDTO = new OrdersPageQueryDTO();
        pageQueryDTO.setStatus(status);
        pageQueryDTO.setUserId(BaseContext.getCurrentId());

        Page<Orders> page = orderMapper.pageQuery(pageQueryDTO);

        long total = page.getTotal();
        List<Orders> orders = page.getResult();
        List<OrderVO> res = new ArrayList<>();
        if(!orders.isEmpty()){
            for (Orders order : orders) {
                Long orderId = order.getId();
                List<OrderDetail> details = orderDetailMapper.getByOrderId(orderId);
                //初始化vo
                OrderVO orderVO  = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDetailList(details);
                //添加到list
                res.add(orderVO);
            }
        }


        return new PageResult(total,res);
    }

    public OrderVO details(Long id) {
        //首先获取订单
        Orders order = orderMapper.getById(id);

        //获取订单详细
        Long orderId = order.getId();
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orderId);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(details);

        return orderVO;
    }

    public void cancel(Long id) {
        //删除订单
        orderMapper.deleteById(id);
        //删除订单中包含的菜品
        orderDetailMapper.deleteByOrderId(id);
    }

    public void repetition(Long id) {
        //获取订单详细菜品
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = details.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;}
        ).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);
        return ;
    }
}
