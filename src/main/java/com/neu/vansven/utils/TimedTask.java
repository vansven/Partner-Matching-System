package com.neu.vansven.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.vansven.domain.User;
import com.neu.vansven.mapper.UserMapper;
import com.neu.vansven.service.UserService;
import com.neu.vansven.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TimedTask {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    private List<Integer> mainUserId = Arrays.asList(1,7);

    @Autowired
    private RedissonClient redissonClient;

    @Scheduled(cron=" * 0/10 * * * ? ")
    public void doPreCache() {
        // redis 存储的 lock 键值对，一个线程对应一把 lock
        String lockKey = "partner:tasktime:lock";
        RLock lock = redissonClient.getLock(lockKey);
        System.out.println("当前运行线程是：" + Thread.currentThread().getName());
        try {
            //waittime一定设置为0，指定线程没抢到后等待指定时长继续抢，因为其他线程在今天没抢到后就不必等待抢占了
            // 无参数或者指定过期时间为-1才会开启看门狗机制实现自动续期
            lock.lock();
            Thread.sleep(400000); //模拟业务操作时间超过指定的30秒时间，会自动续期操作
            System.out.println("当前持有锁的线程是：" + Thread.currentThread().getName());
            // 比如第1页的20条数据是热点数据
            Page<User> page = new Page<>(1,20);
            List<User> hotData = userMapper.selectPage(page, null).getRecords();
            //redis缓存重点用户访问的热点数据
            String userId = "partner:use:searchdata:%d";
            ValueOperations<String, List<User>> ops = redisTemplate.opsForValue();
            for(Integer userid:mainUserId) {
                String user = String.format(userId, userid);
                ops.set(user, hotData,120, TimeUnit.SECONDS); //设置120s的过期时间
            }
        } catch (Exception e) {
            log.error("docacheError",e);
        } finally {
            if(lock.isHeldByCurrentThread()){
                System.out.println("当前释放锁的线程是：" + Thread.currentThread().getName());
                lock.unlock();
            }
        }
    }

    //模拟项目部署在多个服务器上，控制定时任务只有一个线程（服务器）在同一时间开启
//    @Scheduled(cron=" 0/10 * * * * ? ")
//    public void doPreCache() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("当前线程是：" + Thread.currentThread().getName());
//                // redis 缓存
//                String lockKey = "partner:tasktime:lock";
//                RLock lock = redissonClient.getLock(lockKey);
//                try {
//                    //waittime一定设置为0，指定线程没抢到后等待指定时长继续抢，因为其他线程在今天没抢到后就不必等待抢占了
//                    // 无参数或者指定过期时间为-1才会开启看门狗机制实现自动续期
//                    lock.lock();
//                    System.out.println("当前锁持有的线程是：" + Thread.currentThread().getName());
//                    Thread.sleep(40000);
//                    // 比如第1页的20条数据是热点数据
//                    Page<User> page = new Page<>(1,20);
//                    List<User> hotData = userMapper.selectPage(page, null).getRecords();
//                    //redis缓存重点用户访问的热点数据
//                    String userId = "partner:use:searchdata:%d";
//                    ValueOperations ops = redisTemplate.opsForValue();
//                    for(Integer userid:mainUserId) {
//                        String user = String.format(userId, userid);
//                        try{
//                            ops.set(user, hotData,120, TimeUnit.SECONDS); //设置120s的过期时间
//                        }catch (Exception e){
//                            log.error("redis set key error",e);
//                        }
//                    }
//                } catch (Exception e) {
//                    log.error("docacheError",e);
//                } finally {
////                    System.out.println("当前锁释放的线程是" + Thread.currentThread().getName());
////                    lock.unlock();
//                    if(lock.isHeldByCurrentThread()){
//                        System.out.println("当前锁释放的线程是" + Thread.currentThread().getName());
//                        lock.unlock();
//                    }
//                }
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("当前线程是：" + Thread.currentThread().getName());
//                // redis 缓存
//                String lockKey = "partner:tasktime:lock";
//                RLock lock = redissonClient.getLock(lockKey);
//                try {
//                    //waittime一定设置为0，指定线程没抢到后等待指定时长继续抢，因为其他线程在今天没抢到后就不必等待抢占了
//                    // 无参数或者指定过期时间为-1才会开启看门狗机制实现自动续期
//                    lock.lock();
//                        System.out.println("当前锁持有的线程是：" + Thread.currentThread().getName());
//                        Thread.sleep(40000);
//                        // 比如第1页的20条数据是热点数据
//                        Page<User> page = new Page<>(1,20);
//                        List<User> hotData = userMapper.selectPage(page, null).getRecords();
//                        //redis缓存重点用户访问的热点数据
//                        String userId = "partner:use:searchdata:%d";
//                        ValueOperations ops = redisTemplate.opsForValue();
//                        for(Integer userid:mainUserId) {
//                            String user = String.format(userId, userid);
//                            try{
//                                ops.set(user, hotData,120, TimeUnit.SECONDS); //设置120s的过期时间
//                            }catch (Exception e){
//                                log.error("redis set key error",e);
//                            }
//                        }
//                } catch (Exception e) {
//                    log.error("docacheError",e);
//                } finally {
////                    System.out.println("当前锁释放的线程是" + Thread.currentThread().getName());
////                    lock.unlock();
//                    if(lock.isHeldByCurrentThread()){
//                        System.out.println("当前锁释放的线程是" + Thread.currentThread().getName());
//                        lock.unlock();
//                    }
//                }
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("当前线程是：" + Thread.currentThread().getName());
//                // redis 缓存
//                String lockKey = "partner:tasktime:lock";
//                RLock lock = redissonClient.getLock(lockKey);
//                try {
//                    //waittime一定设置为0，指定线程没抢到后等待指定时长继续抢，因为其他线程在今天没抢到后就不必等待抢占了
//                    // 无参数或者指定过期时间为-1才会开启看门狗机制实现自动续期
//                    lock.lock();
//                        System.out.println("当前锁持有的线程是：" + Thread.currentThread().getName());
//                        Thread.sleep(40000);
//                        // 比如第1页的20条数据是热点数据
//                        Page<User> page = new Page<>(1,20);
//                        List<User> hotData = userMapper.selectPage(page, null).getRecords();
//                        //redis缓存重点用户访问的热点数据
//                        String userId = "partner:use:searchdata:%d";
//                        ValueOperations ops = redisTemplate.opsForValue();
//                        for(Integer userid:mainUserId) {
//                            String user = String.format(userId, userid);
//                            try{
//                                ops.set(user, hotData,120, TimeUnit.SECONDS); //设置120s的过期时间
//                            }catch (Exception e){
//                                log.error("redis set key error",e);
//                            }
//                        }
//
//                } catch (Exception e) {
//                    log.error("docacheError",e);
//                } finally {
////                    System.out.println("当前锁释放的线程是" + Thread.currentThread().getName());
////                    lock.unlock();
//                    if(lock.isHeldByCurrentThread()){
//                        System.out.println("当前锁释放的线程是" + Thread.currentThread().getName());
//                        lock.unlock();
//                    }
//                }
//            }
//        }).start();
//
//    }



}
