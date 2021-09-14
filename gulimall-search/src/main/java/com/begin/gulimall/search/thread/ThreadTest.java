package com.begin.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {
    public static ExecutorService execute = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("main.......start..");
//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//        }, execute);

        /**
         * 方法完成后的感知
         * */
//        CompletableFuture<Integer> supplyAsync = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, execute).whenComplete((res,exception)->{
//            System.out.println("异步任务完成了。。。结果是："+res+";异常是："+exception);
//        }).exceptionally((throwable -> {
//            return  10;
//        }));

        /**
         * 方法完成后的处理
         */
//        CompletableFuture<Integer> supplyAsync = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, execute).handle((res, exception) -> {
//            if (res != null) {
//                return res * 2;
//            }
//            if (exception != null) {
//                return 0;
//            }
//            return 0;
//        });

        /**
         * 线程串行换
         * 1)、thenRun:不能获取到上一步的执行结果，无返回值
         *2）、thenAcceptAsync：能接受到上一步结果，但是无返回值
         * 3)、thenApplyAsync：能接上到上一步结果，有返回值
         */
//        CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, execute).thenApplyAsync((t)->{
//            System.out.println("任务二启动了。。。 ");
//            return 10;
//        },execute);

        /**
         * 两个都完成
         *
         */
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务一线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运任务一行结果：" + i);
            return i;
        }, execute);

        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务二线程：" + Thread.currentThread().getId());
            System.out.println("运任务二行结束");
            return "Hello";
        }, execute);

//        future01.runAfterBothAsync(future02,()->{
//            System.out.println("任务三开始...");
//        },execute);

        future01.thenAcceptBothAsync(future02,(f1,f2)->{
            System.out.println("任务三开始。。。之前的结果："+f1+"-->"+f2);
        },execute);

        CompletableFuture<String> completableFuture = future01.thenCombineAsync(future02, (f1, f2) -> {
            System.out.println("任务三开始。。。之前的结果：" + f1 + "-->" + f2);
            return f1 + f2;
        }, execute);
//        Integer integer = supplyAsync.get();
        System.out.println("main.......end..");
    }


    public void thread(String[] args) throws ExecutionException, InterruptedException {
        /**
         * 1）、继承Thread
         * Thread01 thread01 = new Thread01();
         *         thread01.start();
         *2）、实现Runnable接口
         * Runnable01 runnable01 = new Runnable01();
         *         Thread thread = new Thread(runnable01);
         *         thread.start();
         *3)、实现Callable接口+FutureTask（可以拿到返回结果，可以处理异常）
         *    FutureTask<Integer> task = new FutureTask<>(new Callable01());
         *         Thread thread = new Thread(task);
         *         thread.start();
         *         //阻塞等待整个线程执行完成，获取返回结果
         *         Integer integer = task.get();
         * 4)、线程池
         * 给线程池直接提交任务
         *
         */
        System.out.println("main.......start..");
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor();

        //当前系统中线程池只有一两个，每个异步任务，提交给线程池让他自己去执行
//        service.execute(new Runnable01());


        System.out.println("main.......end..");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }

}
