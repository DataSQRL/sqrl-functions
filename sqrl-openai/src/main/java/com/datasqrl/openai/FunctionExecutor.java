package com.datasqrl.openai;

import com.datasqrl.openai.util.FunctionMetricTracker;
import org.apache.flink.table.functions.FunctionContext;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

public class FunctionExecutor {

    private final FunctionMetricTracker metricTracker;

    public FunctionExecutor(FunctionContext context, String functionName) {
        this.metricTracker = new FunctionMetricTracker(context, functionName);
    }

    public <T> T execute(Callable<T> task) {
        metricTracker.increaseCallCount();

        final long start = System.nanoTime();

        final T ret = executeWithRetry(
                task,
                metricTracker::increaseErrorCount,
                metricTracker::increaseRetryCount
        );

        final long elapsedTime = System.nanoTime() - start;
        metricTracker.recordLatency(TimeUnit.NANOSECONDS.toMillis(elapsedTime));

        return ret;
    }
}
