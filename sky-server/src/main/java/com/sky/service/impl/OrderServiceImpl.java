package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContextByMe;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzmr
 * @create 2023-09-08 9:12
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 检查客户的收货地址是否超出配送范围
     *
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", ak);

        // 获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("店铺地址解析失败");
        }

        // 数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        // 店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address", address);
        // 获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("收货地址解析失败");
        }

        // 数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        // 用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin", shopLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info", "0");

        // 路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("配送路线规划失败");
        }

        // 数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if (distance > 5000) {
            // 配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }


    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        // 处理各种异常（地址簿为空，购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            // 地址为空，抛出错误信息
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }


        // 检查用户的收货地址是否超出配送范围
        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());


        Long userId = BaseContextByMe.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.getByUserId(userId);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            // 没有查到购物车数据，购物车为空
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // =================

        // 向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        // 设置订单状态为待付款
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        // 地址应该是所有地址拼一块吧
        orders.setAddress(addressBook.getProvinceName() + addressBook.getDistrictName() + addressBook.getCityName() + addressBook.getDetail());
        // 插入之后会将主键返回
        orderMapper.insert(orders);
        // 向订单明细表插入n条数据  由购物车的数据决定

        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            // 将购物车的数据拷贝给orderDetail
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        // 批量插入订单详情数据
        orderDetailMapper.insert(orderDetailList);

        // 清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        // 封装返回结果
        OrderSubmitVO orderSubmitVO =
                OrderSubmitVO.builder().id(orders.getId()).orderTime(orders.getOrderTime()).orderNumber(orders.getNumber()).orderAmount(orders.getAmount()).build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContextByMe.getCurrentId();
        User user = userMapper.getById(userId);

        // 调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), // 商户订单号
                new BigDecimal(0.01), // 支付金额，单位 元
                "苍穹外卖订单", // 商品描述
                user.getOpenid() // 微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 模拟的支付，直接支付成功就完了
     *
     * @param ordersPaymentDTO
     */
    @Override
    public void paymentWithNoMoney(OrdersPaymentDTO ordersPaymentDTO) {
        // 获取到订单号
        String orderNumber = ordersPaymentDTO.getOrderNumber();

        // 可能要加个付款时间
        LocalDateTime checkoutTime = LocalDateTime.now();

        // 直接修改订单状态
        orderMapper.changeStatusAndCheckoutTime(orderNumber, checkoutTime);


        // 要获取个订单id才行，根据订单号获取订单id
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());

        // 通过webSocket向客户端浏览器推送消息 type,orderId,content
        Map map = new HashMap<>();
        map.put("type", 1); // 1表示来单提醒
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + orderNumber);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

    }

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContextByMe.getCurrentId());

        Page<Orders> page = orderMapper.list(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        // 问题出在没有查出来订单明细,这个要封装到OrderVo中才行
        if (page != null && page.size() > 0) {
            for (Orders orders : page) {
                // 根据订单id查询订单明细
                Long ordersId = orders.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(ordersId);
                OrderVO orderVO = new OrderVO();

                // 我不太明白这一步是干什么?明明orderVO只能两个字段,还有一个字符串,属性名和orders里还没有对应上的.
                // 奶奶滴,原来这里是继承!!!!!
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    @Override
    public OrderVO getOrderDetail(Long id) {

        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        // 将查询到的Order信息赋值给OrderVO,因为OrderVO中可以封装订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;

    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void userCancelById(Long id) {

        // 所以要先判断 当前订单的状态-根据订单id来查询
        Orders orders = orderMapper.getById(id);

        // 先判断订单是否为空
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 然后就是判断状态了,这里直接判断状态是否大于2,如果大于2就不让取消订单,虽然理应是协商是否能退
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 剩下的情况就是正常了,能够退款  1 待支付 2 待接单

        // 退款的接口肯定写不了了,就放着吧

        // 待支付和待接单状态下，用户可直接取消订单
        Orders cancelOrder =
                Orders.builder().id(id).status(Orders.CANCELLED)
                        .cancelReason("用户取消").cancelTime(LocalDateTime.now()).build();

        orderMapper.update(cancelOrder);
    }

    /**
     * 再次下单
     *
     * @param id
     */
    @Override
    public void repetitionOrder(Long id) {

        // 那整体步骤就是
        // 1.根据订单id查询订单详情
        // 2.将订单详情的每一项都赋值给一个购物车，然后将该购物车数据加入数据库

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 有了订单详情，也有了订单的基本信息
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContextByMe.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    /**
     * 管理端订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 开启分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // 这个应该就不需要用户id了，直接查看所有用户的

        Page<Orders> page = orderMapper.list(ordersPageQueryDTO);

        // 还要再封装 订单详情的信息  这里的page是所有的订单！？

        List<OrderVO> list = new ArrayList<>();

        if (page != null && page.size() > 0) {
            for (Orders orders : page) {
                // 拿到订单id，根据订单id查询订单详情
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                // 这个要拼接一下
                orderVO.setOrderDishes(getOrderDishes(orders));
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 各个状态的订单数量统计
     * 待接单，待派送，派送中
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        /*OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(0);
        orderStatisticsVO.setConfirmed(0);
        orderStatisticsVO.setDeliveryInProgress(0);
        // 查询所有的订单
        List<Orders> list = orderMapper.getAllOrders();

        // 遍历整个订单集合
        for (Orders orders : list) {
            Integer status = orders.getStatus();
            if (status == Orders.TO_BE_CONFIRMED) {
                orderStatisticsVO.setToBeConfirmed(orderStatisticsVO.getToBeConfirmed() + 1);
            }
            if (status == Orders.CONFIRMED) {
                orderStatisticsVO.setConfirmed(orderStatisticsVO.getConfirmed() + 1);
            }
            if (status == Orders.DELIVERY_IN_PROGRESS) {
                orderStatisticsVO.setDeliveryInProgress(orderStatisticsVO.getDeliveryInProgress() + 1);
            }
        }

        return orderStatisticsVO;*/

        //=================

        // 上面的代码太麻烦了
        // 根据状态-分别查询出待接单，带派送，派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 封装数据
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 接单
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        // 设置id和状态为已接单
        Orders orders = Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
        // 调用之前的动态sql来修改
        orderMapper.update(orders);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        // 其实就是设置状态为已取消
        // 还要先判断是否订单已支付
        Integer status = orderMapper.getById(ordersRejectionDTO.getId()).getStatus();
        log.info("当前状态为:{}", status);
        Orders orders =
                Orders.builder().id(ordersRejectionDTO.getId()).status(Orders.CANCELLED).cancelTime(LocalDateTime.now())
                        .rejectionReason(ordersRejectionDTO.getRejectionReason()).build();
        orderMapper.update(orders);
    }

    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {

        // 退款的代码依然不用写  那也写一下判断吧
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
        if (ordersDB.getPayStatus() == 1) {
            log.info("退款中....");
        }


        Orders orders =
                Orders.builder().id(ordersCancelDTO.getId()).cancelTime(LocalDateTime.now())
                        .cancelReason(ordersCancelDTO.getCancelReason()).status(Orders.CANCELLED).build();

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {

        // 判断状态
        if (orderMapper.getById(id).getStatus() != Orders.CONFIRMED) {
            // 只有这个已接单的状态才能派送
            throw new OrderBusinessException("订单状态错误");
        }

        // 修改派送的订单的订单状态
        Orders orders = Orders.builder().id(id).status(Orders.DELIVERY_IN_PROGRESS).build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orderDB = orderMapper.getById(id);
        // 只有派送中的订单才能完成
        if (orderDB == null || orderDB.getStatus() != Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException("订单状态错误");
        }
        Orders orders = Orders.builder().id(id).status(Orders.COMPLETED).build();
        orderMapper.update(orders);
    }

    /**
     * 用户催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        // 先根据订单id查询到订单号
        Orders orderDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        String number = orderDB.getNumber();

        // 封装返回信息
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "用户催单:" + number);
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }


    private String getOrderDishes(Orders orders) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
        // 将每一条菜品的信息拼接成一个字符串
        StringBuffer stringBuffer = new StringBuffer();
        for (OrderDetail orderDetail : orderDetails) {
            String orderDish = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
            stringBuffer.append(orderDish);
        }
        return stringBuffer.toString();
    }

}
