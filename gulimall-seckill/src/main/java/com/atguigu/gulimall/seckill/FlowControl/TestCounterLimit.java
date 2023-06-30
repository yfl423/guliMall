package com.atguigu.gulimall.seckill.FlowControl;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 计数器限流算法实现
 */
public class TestCounterLimit {
    static CounterLimit counterLimit = new CounterLimit(100, 1000, TimeUnit.MILLISECONDS);

    public static void main(String[] args) {
        if (counterLimit.canPass()) {
            //
        }

    }
}


class CounterLimit {
    long upperLimit;
    long timeInterval;
    TimeUnit timeUnit;
    AtomicLong counter;

    public CounterLimit(long upperLimit, long timeInterval, TimeUnit timeUnit) {
        this.upperLimit = upperLimit;
        this.timeInterval = timeInterval;
        this.timeUnit = timeUnit;
        counter = new AtomicLong();
        startResetTask();
    }

    private void startResetTask() {
        ScheduledExecutorService executorService;
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(() -> {
            counter.set(0);
        }, 0, timeInterval, timeUnit);
    }

    public boolean canPass() {
        if (counter.incrementAndGet() > upperLimit) {
            throw new RuntimeException();
        }
        return true;
    }
}
