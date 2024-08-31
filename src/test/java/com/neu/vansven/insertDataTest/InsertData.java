package com.neu.vansven.insertDataTest;
import java.util.ArrayList;
import java.util.concurrent.*;

import com.neu.vansven.domain.User;
import com.neu.vansven.mapper.UserMapper;
import com.neu.vansven.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

@Slf4j
@SpringBootTest
public class InsertData {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    private StopWatch stopWatch = new StopWatch();

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(400,1000,200,
            TimeUnit.SECONDS,new ArrayBlockingQueue<>(200),new ThreadPoolExecutor.AbortPolicy());

    @Test
    void singleInsert(){

        ArrayList<User> addUserList = new ArrayList<>();
        stopWatch.start();

        // 单线程批量插入10，0000条数据，每批插入 1.000条数据
        for(int i = 0; i < 100000; i++){
            User addUser = new User();
            addUser.setUserName("傻子");
            addUser.setUserAccount("xxxxiA_lover");
            addUser.setAvatarUrl("");
            addUser.setPassword("1666252833333");
            addUser.setPhone("");
            addUser.setEmail("");
            addUser.setUserRole(0);
            addUser.setTags("[\"go\",\"男\",\"考研\",\"应届生\",\"25届毕业\"]");
            addUserList.add(addUser);
        }
        userService.saveBatch(addUserList,1000);
        stopWatch.stop();
        // 14176
        System.out.println("单线程批量插入10，0000条数据总耗时：" + stopWatch.getTotalTimeMillis() );
    }

    @Test
    void onceInsert(){

        stopWatch.start();
        // sql语句一条一条插入10，0000条数据
        for(int i = 0; i < 100000; i++){
            User user = new User();
            user.setUserName("呆子");
            user.setUserAccount("vans");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setPassword("12444466666");
            user.setPhone("");
            user.setEmail("");
            user.setUserRole(0);
            user.setTags("[\"php\",\"python\",\"c++\",\"jsp\"]");
            userMapper.insert(user);
        }

        stopWatch.stop();
        // 79369
        System.out.println("sql一个一个插入10,0000数据总耗时" + stopWatch.getTotalTimeMillis() );
    }

    @Test
    void multipleInsert() throws ExecutionException, InterruptedException {


        ArrayList<CompletableFuture<Void>> futureList = new ArrayList<>();


        stopWatch.start();
        // 多线程批量插入：开多个线程，每次线程批量插入10，0000条数据，每批插入 1.000条数据
        for(int i = 0; i < 10; i++){
            // 保证按照先后顺序，每次插入的批量数据都会重新创建一个 addUserList集合 存储
            // 第一组先插入 0 – 9999数据，
            // 第二组再插入 10000 – 19999 数据，
            // 第三组再插入 20000 – 29999 数据
            // ......
            ArrayList<User> addUserList = new ArrayList<>();
            for(int j = 0; j< 10000; j++){
                User user = new User();
                user.setUserName("小小虾米");
                user.setUserAccount("van_xxxiA");
                user.setAvatarUrl("");
                user.setGender(0);
                user.setPassword("166627278838");
                user.setPhone("");
                user.setEmail("");
                user.setUserRole(0);
                user.setTags("[\"php\",\"jsp\"]");
                addUserList.add(user);
            }
            // 异步执行
            CompletableFuture<Void> completableFutureTask = CompletableFuture.runAsync(() -> {
                userService.saveBatch(addUserList, 1000);
                System.out.println("threadName：" + Thread.currentThread().getName());
            },threadPoolExecutor);
            futureList.add(completableFutureTask);
        }
        // 组合这10个 每次批量插入1000个数据 异步任务combineMultiple
        // combineMultiple任务 只有多个任务都执行完成后才会执行，只有有一个任务执行异常，则返回的CompletableFuture执行get方法时会抛出异常，如果都是正常执行，则get返回null。
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).get();
        stopWatch.stop();

        // 3142
        System.out.println("多线程批量插入10,0000数据总耗时" + stopWatch.getTotalTimeMillis() );
    }

    @Test
    void  ayscnTest() throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //任务执行过程
            }
        }; // runn

        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                // 任务执行过程
                return null;
            }
        };
        Integer getResult = callable.call(); //获取任务执行后的结果

        FutureTask<Integer> task = new FutureTask<Integer>(runnable,0);
    }



}
