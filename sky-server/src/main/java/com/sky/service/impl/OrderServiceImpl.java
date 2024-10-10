package com.sky.service.impl;

import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;

    @Value("${sky.baidu.coordinate}")
    private String coordinateUrl;

    private void checkOutOfRange(String address){
        Map<String,String> map = new HashMap<>();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //调用这个api会将对应的地址转换成经纬度
        //自动将json数据处理成对应字符串。
        String shopCoordinate = HttpClientUtil.doGet(coordinateUrl, map);

        //转换成对象
        JSONObject jsonObject = JSON.parseObject(shopCoordinate);

        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("商家地址解析失败！");
        }

        //获取经纬度
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");

        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //根据经纬度计算出距离

        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        String userCoordinate = HttpClientUtil.doGet(coordinateUrl, map);
        jsonObject = JSON.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("收货地址解析失败！");
        }

        JSONObject userLocation = jsonObject.getJSONObject("result").getJSONObject("location");
        String userLng = userLocation.getString("lng");
        String userLat = userLocation.getString("lat");

        String userLngLat = userLat + "," + userLng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        String s = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);
        jsonObject = JSON.parseObject(s);

        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("路径解析失败！");
        }
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray)result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");



        if(distance < 5000) throw new OrderBusinessException("超出配送范围");

    }



    public OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO) {

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null) throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

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

    public void userCancelById(Long id) throws Exception {
        Orders ordersDB = orderMapper.getById(id);

        //订单id不存在
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //状态不正确
        if(ordersDB.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            orders.setPayStatus(Orders.REFUND);
        }


        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
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

    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Long id = ordersRejectionDTO.getId();

        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Integer payStatus = ordersDB.getPayStatus();
        if(payStatus == Orders.PAID){
            //进行退款

            log.info("用户退款:{}",ordersDB.getNumber());
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());

        orderMapper.update(orders);
    }

    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
        Integer payStatus = ordersDB.getStatus();

        if(payStatus == 1){
//            调用微信支付的api，第一个参数传入用户的订单号，第二个为退款订单号，第三个为退款金额的精确度，第四个为金额的精确度
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01)
            );
            log.info("用户退款:{}",refund);
        }
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    public void delivery(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    public void complete(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null || ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setDeliveryTime(LocalDateTime.now());
        orders.setStatus(Orders.COMPLETED);

        orderMapper.update(orders);
    }

    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    public OrderStatisticsVO statistics() {

        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        return null;
    }

    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        JSONObject jsonObject = new JSONObject();
        if(jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")){
            throw new OrderBusinessException("订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    public void paySuccess(String orderNumber) {
         //直接修改订单状态
        Long userId = BaseContext.getCurrentId();
        Orders ordersDB = orderMapper.getByNumberAndUserId(orderNumber, userId);

        Orders newOrders = Orders.builder().id(ordersDB.getId())
        //在企业开发中，这里设置订单状态应该为待接单，但是为了体现Apache Echarts
                .status(Orders.COMPLETED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(newOrders);

        Map map = new HashMap<>();
        map.put("type",1);//1表示来单提醒2表示催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号" + orderNumber);

        String s = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(s);

        return ;
    }

    public void reminder(Long id) {

        Orders ordersDB = orderMapper.getById(id);

        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap<>();
        map.put("type",2);  //2表示催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号" + ordersDB.getNumber());

        //JSON方法通使用场景更加广泛，而JSONObject的该方法更多在对象示例中调用。
        String s = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(s);
        return ;
    }


}
