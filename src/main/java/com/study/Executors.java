package com.study;

import java.util.concurrent.ExecutorService;

public class Executors {
    int capacity;

    public static ExecutorService newFixedThreadPool() {
        return new ThreadPoolExecutor();
    }
    public static ExecutorService newFixedThreadPool(int capacity) {
        return new ThreadPoolExecutor();
    }
    public static ExecutorService newFixedThreadPool(int capacity, int queue) {
        return new ThreadPoolExecutor(capacity, queue);
    }
}
