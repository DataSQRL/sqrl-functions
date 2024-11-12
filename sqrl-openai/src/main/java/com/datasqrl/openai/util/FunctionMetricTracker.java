package com.datasqrl.openai.util;

import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.table.functions.FunctionContext;

public class FunctionMetricTracker {

    public static final String P99_METRIC = "com.datasqrl.openai.%s.p99";
    public static final String CALL_COUNT = "com.datasqrl.openai.%s.callCount";
    public static final String ERROR_COUNT = "com.datasqrl.openai.%s.errorCount";
    public static final String RETRY_COUNT = "com.datasqrl.openai.%s.retryCount";

    private final P99LatencyTracker latencyTracker = new P99LatencyTracker();
    private final Counter callCount;
    private final Counter errorCount;
    private final Counter retryCount;

    public FunctionMetricTracker(FunctionContext context, String functionName) {
        final String p99MetricName = String.format(P99_METRIC, functionName);
        final String callCountName = String.format(CALL_COUNT, functionName);
        final String errorCountName = String.format(ERROR_COUNT, functionName);
        final String retryCountName = String.format(RETRY_COUNT, functionName);

        context.getMetricGroup().gauge(p99MetricName, (Gauge<Long>) latencyTracker::getP99Latency);
        callCount = context.getMetricGroup().counter(callCountName);
        errorCount = context.getMetricGroup().counter(errorCountName);
        retryCount = context.getMetricGroup().counter(retryCountName);
    }

    public void increaseCallCount() {
        callCount.inc();
    }

    public void increaseErrorCount() {
        errorCount.inc();
    }

    public void increaseRetryCount() {
        retryCount.inc();
    }

    public void recordLatency(long latencyMs) {
        latencyTracker.recordLatency(latencyMs);
    }

}
