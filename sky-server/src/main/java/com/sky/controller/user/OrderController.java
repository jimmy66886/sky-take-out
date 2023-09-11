package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zzmr
 * @create 2023-09-08 9:09
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("下单的数据：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    // 导入的代码

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        // OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        // log.info("生成预支付交易单：{}", orderPaymentVO);

        // 直接返回，也是不行啊，里面的这些内容都没有

        // 直接根据订单号来修改订单状态，只要用户发了请求就是订单完成付款
        // 是设置订单支付状态为已支付，是1，订单状态设置为待接待2
        orderService.paymentWithNoMoney(ordersPaymentDTO);

        return Result.success();
    }

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单分页查询")
    public Result<PageResult> getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单分页查询");
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {

        // 传来的是orderId,要根据orderId先在order表中查到订单的基本信息,然后在order_detail表中查到详细信息,也就是菜品啥的

        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) {
        log.info("取消订单id为:{}", id);
        orderService.userCancelById(id);
        return Result.success();
    }

    /**
     * 再来一单就是将原订单中的商品重新加入到购物车中
     *
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetitionOrder(@PathVariable Long id) {
        log.info("要重新下单的订单id：{}", id);
        orderService.repetitionOrder(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id) {
        // 用户催单,服务端接收到催单请求,然后再提醒浏览器
        orderService.reminder(id);
        return Result.success();
    }

}
