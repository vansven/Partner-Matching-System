//package com.neu.vansven.importdata;
//
//import com.neu.vansven.domain.User;
//import com.neu.vansven.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//
//@Component
//public class InsertUsers {
//
//    @Autowired
//    private static UserService userService;
//
//    public static void main(String[] args) {
//        ArrayList<CompletableFuture<Void>> futureList = new ArrayList<>();
//        // 多线程批量插入：开多个线程，每次线程批量插入10，0000条数据，每批一次性插入 1.000条数据
////        UserService userService = new UserServiceImpl();
//        System.out.println("userService的值为：" + userService);
//        for (int i = 0; i < 10; i++) {
//            // 保证按照先后顺序，每次插入的批量数据都会重新创建一个 addUserList集合 存储
//            // 第一组先插入 0 – 9999数据，
//            // 第二组再插入 10000 – 19999 数据，
//            // 第三组再插入 20000 – 29999 数据
//            // ......
//            ArrayList<User> addUserList = new ArrayList<>();
//            for (int j = 0; j < 10000; j++) {
//                User addUser = new User();
//                addUser.setUserName("傻子");
//                addUser.setUserAccount("xxxxiA_lover");
//                addUser.setPassword("1666252833333");
//                addUser.setTags("[\"go\",\"php\",\"jsp\"]");
//                addUserList.add(addUser);
//            }
//            // 异步执行
//            CompletableFuture<Void> completableFutureTask = CompletableFuture.runAsync(() -> {
//                userService.saveBatch(addUserList, 1000);
//                System.out.println("threadName：" + Thread.currentThread().getName());
//            });
//            futureList.add(completableFutureTask);
//        }
//
//        // CompletableFuture 组合多任务处理，先组合多个任务然后调用阻塞方法实现多个插入任务结束后再执行后面的程序
//        try {
//            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//}
