package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-10 19:56
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单的方法
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        // time这个,老师用的是plusMinutes(-15),那为什么不直接用minus呢?

        // 查询超时订单 select * from orders where status = ? and order_time < (当前时间-15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,
                LocalDateTime.now().minusMinutes(15));

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                // 设置状态为取消
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 每天凌晨一点触发
     * 处理一直处于派送中的订单-自动完成
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理一直处于派送中的订单:{}", LocalDateTime.now());

        // 当前时间 一点,减去一个小时,得到就是0点,可以用来整理昨天的订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,
                LocalDateTime.now().minusHours(1));

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                // 设置状态为取消
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

}
