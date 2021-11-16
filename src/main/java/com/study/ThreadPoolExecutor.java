package com.study;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Slf4j
public class ThreadPoolExecutor extends AbstractExecutorService {
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;
    private static final int DEFAULT_QUEUE_SIZE = 10;
    private final ReentrantLock mainThreadLock = new ReentrantLock();
    private final ArrayBlockingQueue<Runnable> taskQueue;
    private final List<Worker> workers;
    private final Condition termination = mainThreadLock.newCondition();
    private volatile boolean isTerminated;
    private boolean isAllowed = true;

    public ThreadPoolExecutor() {
        this(DEFAULT_THREAD_POOL_SIZE, DEFAULT_QUEUE_SIZE);
    }

    public ThreadPoolExecutor(int numberOfThreads) {
        this(numberOfThreads, DEFAULT_QUEUE_SIZE);
    }

    public ThreadPoolExecutor(int numberOfThreads, int queueSize) {
        this.taskQueue = new ArrayBlockingQueue<>(queueSize);
        workers = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            new Thread(worker).start();
        }
    }

    @Override
    public void shutdown() {
        mainThreadLock.lock();
        interruptWorkers();
        isTerminated = true;
        mainThreadLock.unlock();
    }

    @Override
    public List<Runnable> shutdownNow() {
        mainThreadLock.lock();
        interruptWorkers();
        List<Runnable> list = new ArrayList<>();
        taskQueue.drainTo(list);
        isTerminated = true;
        mainThreadLock.unlock();
        return list;
    }

    @Override
    public boolean isShutdown() {
        return workers.stream()
                .filter(worker -> worker.state == State.INTERRUPTED)
                .count() == workers.size();
    }

    @Override
    public boolean isTerminated() {
        return isTerminated;
    }

    @SneakyThrows
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        long nanos = unit.toNanos(timeout);
        mainThreadLock.lock();
        isAllowed = false;
        int stateSum = workers.stream().mapToInt(worker -> worker.state.getValue()).sum();
        try {
            while (stateSum > 0) {
                if (nanos <= 0L)
                    return false;
                nanos = termination.awaitNanos(nanos);
                stateSum = workers.stream().mapToInt(worker -> worker.state.getValue()).sum();
            }
            return true;
        } finally {
            mainThreadLock.unlock();
        }
    }

    @SneakyThrows
    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException("Command cannot be null");
        }
        if (isAllowed) {
            taskQueue.put(command);
        }
    }

    private void interruptWorkers() {
        for (Worker w : workers)
            w.stop();
    }

    private final class Worker implements Runnable {
        private Thread currentThread;
        private volatile State state = State.IDLE;

        @Override
        public void run() {
            this.currentThread = Thread.currentThread();
            while (!currentThread.isInterrupted()) {
                try {
                    Runnable runnable = taskQueue.take();
                    state = State.RUNNING;
                    runnable.run();
                } catch (InterruptedException e) {
                    log.error("Worker error while take()", e);
                }
                state = State.IDLE;
            }
            state = State.INTERRUPTED;
        }

        public void stop() {
            currentThread.interrupt();
        }
    }
}
