package com.datasqrl.openai;

import com.datasqrl.openai.util.FunctionMetricTracker;
import org.apache.flink.table.functions.FunctionContext;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

public class FunctionExecutor {

    private final FunctionMetricTracker metricTracker;
    private final ExecutorService executorService;

    public FunctionExecutor(FunctionContext context, String functionName) {
        this.metricTracker = new FunctionMetricTracker(context, functionName);
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public <T> CompletableFuture<T> executeAsync(Callable<T> task) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                metricTracker.increaseCallCount();
                final long start = System.nanoTime();

                T result = executeWithRetry(
                        task,
                        metricTracker::increaseErrorCount,
                        metricTracker::increaseRetryCount
                );

                final long elapsedTime = System.nanoTime() - start;
                metricTracker.recordLatency(TimeUnit.NANOSECONDS.toMillis(elapsedTime));

                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
