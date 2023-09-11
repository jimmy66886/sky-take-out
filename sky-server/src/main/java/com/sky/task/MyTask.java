package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author zzmr
 * @create 2023-09-10 19:37
 * 自定义定时任务类
 */
@Component
@Slf4j
public class MyTask {

    /**
     * 从第0秒开始,每5秒执行一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void executeTask() {
        log.info("定时任务开始执行:{}", new Date());
    }

}