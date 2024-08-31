package com.neu.vansven.redissontest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.vansven.domain.User;
import com.neu.vansven.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class RedissonTest {
    @Autowired
    private RedissonClient redissonClient;


    @Test
    void baseGrammer(){
        RSet<String> van = redissonClient.getSet("van");
        van.add("xxxiA");

    }

    @Test
    void watchDogTest(){
        RLock lock = redissonClient.getLock("partner:test:lock");
        System.out.println("当前线程是：" + Thread.currentThread().getName());
        try {
            if(lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                Thread.sleep(30000);
                System.out.println("当前操作共享资源的线程是：" + Thread.currentThread().getName());

            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                System.out.println("释放锁的线程是：" + Thread.currentThread().getName());
                lock.unlock();
            }
        }
    }

}
