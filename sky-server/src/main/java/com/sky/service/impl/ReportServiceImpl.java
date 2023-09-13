package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zzmr
 * @create 2023-09-11 23:25
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        // 基于begin和end得到区间内的日期，可以无需查询数据库
        List<LocalDate> dateList = getLocalDateList(begin, end);

        // 存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            // 遍历这个日期集合，然后根据每一天的日期查询数据库，返回营业额数据--- 状态为已完成的订单金额合计
            // select sum(amount) from orders where status = ? and order_time > ? and order_time < ?
            // 获取到的是该日期的0点0分0秒和23:59:59.999999999
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            // 三元运算，判断是否为空，如果为空则赋值为0.0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 工具类，取出dateList集合中的每一项，然后以逗号分隔，得到一个String字符串
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        // 要获取当天新增的用户量，当天总用户量，以及日期的时间段？

        List<LocalDate> dateList = getLocalDateList(begin, end);
        // 这样就得到了日期的集合

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        // 那新增的用户/用户总量怎么求呢
        // select count(id) from user where create_time < end and createTime > begin
        // select count(id) from user where create_time < end
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            totalUserList.add(userMapper.countByMap(map));
            map.put("begin", beginTime);
            newUserList.add(userMapper.countByMap(map));
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = getLocalDateList(begin, end);

        // 得到日期集合

        // 要求的数据：有效订单总数  订单总数 来得到完成率  订单数列表 有效订单数列表
        // select count(id) from orders where status = ? and order_time < ?  and order_time >?

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate localDate : dateList) {
            // 根据日期，得到该日期的订单总数和有效订单数
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            // 没有状态条件，查的是该日期的所有订单的数量
            orderCountList.add(getOrderCount(beginTime, endTime, null));
            // 查的是该日期的有效订单数量
            validOrderCountList.add(getOrderCount(beginTime, endTime, Orders.COMPLETED));
        }

        /**
         * 不查数据库，而去遍历集合，即可得到区间内的订单总数量和有效订单数量
         * 使用Stream流更简单
         */
        /*Integer totalOrderCount = 0;
        for (Integer i : orderCountList) {
            totalOrderCount += i;
        }

        Integer validOrderCount = 0;
        for (Integer i : validOrderCountList) {
            validOrderCount += i;
        }*/
        // stream流的形式，只需要两行
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        // 订单完成率 这里又没注意到一个细节，就是总订单数为0的情况
        Double orderCompletionRate = 0.0;
        if (totalOrderCount > 0) {
            orderCompletionRate = (validOrderCount / totalOrderCount.doubleValue());
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量前十
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> top10List = orderMapper.getTop10(beginTime, endTime);

        /**
         * 使用Stream流处理
         */
        List<String> names = top10List.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = top10List.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map timeMap = new HashMap();
        timeMap.put("begin", beginTime);
        timeMap.put("end", endTime);
        timeMap.put("status", status);
        return orderMapper.countOrderByMap(timeMap);
    }

    private List<LocalDate> getLocalDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
