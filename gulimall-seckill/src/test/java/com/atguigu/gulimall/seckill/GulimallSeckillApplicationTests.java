package com.atguigu.gulimall.seckill;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class GulimallSeckillApplicationTests {

    @Test
    public void contextLoads() {
//        LocalDate date = LocalDate.now();
//        LocalTime time = LocalTime.of(0, 0, 0);
//        LocalDateTime startTime = LocalDateTime.of(date, time);
//        System.out.println(startTime.format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")));
//        LocalDate now = LocalDate.now();
//        LocalDate date = now.plusDays(2L);
//        LocalTime time = LocalTime.of(23, 59, 59);
//        LocalDateTime endTime = LocalDateTime.of(date, time);
//        System.out.println(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Date date = new Date();
        System.out.println(date.getTime());
    }

}
