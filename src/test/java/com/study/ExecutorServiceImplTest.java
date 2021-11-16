package com.study;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
class ExecutorServiceImplTest {
    private static int DEFAULT_CAPACITY = 4;

    @Test
    @DisplayName("When create custom ExecutorService check awaitTermination method")
    void myAwaitTermination() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(3, 10);
        for (int i = 0; i < 1000; i++) {
            int taskNo = i;
            threadPool.execute(() -> {
                String message =
                        Thread.currentThread().getName() + ": Task " + taskNo;
                log.info(message);
            });
        }

        log.info(String.valueOf(threadPool.awaitTermination(5000L, TimeUnit.MILLISECONDS)));
        threadPool.shutdown();
    }

    @Test
    @DisplayName("When create original ExecutorService check awaitTermination method")
    void origAwaitTermination() throws InterruptedException {
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(3);

        for (int i = 0; i < 1000000; i++) {
            int taskNo = i;
            threadPool.execute(() -> {
                String message = Thread.currentThread().getName() + ": Task " + taskNo;
                log.warn(message);
            });
        }

        threadPool.awaitTermination(1L, TimeUnit.MILLISECONDS);
        threadPool.shutdown();
    }

    @Test
    @DisplayName("When create custom ExecutorService check awaitTermination method")
    void myAwaitTermination1() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(3, 20);

        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                String message = Thread.currentThread().getName() + ": Task " + finalI;
                log.info(message);
            });
        }

        log.info(String.valueOf(threadPool.awaitTermination(5000L, TimeUnit.MILLISECONDS)));
        threadPool.shutdown();
    }

}