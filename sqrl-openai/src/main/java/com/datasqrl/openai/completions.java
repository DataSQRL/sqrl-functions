package com.datasqrl.openai;

import com.datasqrl.openai.util.P99LatencyTracker;
import com.google.auto.service.AutoService;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.concurrent.TimeUnit;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

@AutoService(ScalarFunction.class)
public class completions extends ScalarFunction {

    public static final String P99_METRIC = "com.datasqrl.openai.completions.p99";
    public static final String CALL_COUNT = "com.datasqrl.openai.completions.callCount";
    public static final String ERROR_COUNT = "com.datasqrl.openai.completions.errorCount";
    public static final String RETRY_COUNT = "com.datasqrl.openai.completions.retryCount";

    private OpenAICompletions openAICompletions;

    private transient P99LatencyTracker latencyTracker;
    private transient Counter callCounter;
    private transient Counter errorCounter;
    private transient Counter retryCounter;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAICompletions = createOpenAICompletions();
        this.latencyTracker = new P99LatencyTracker(100);
        context.getMetricGroup()
                .gauge(P99_METRIC, (Gauge<Long>) () -> latencyTracker.getP99Latency());
        callCounter = context.getMetricGroup().counter(CALL_COUNT);
        errorCounter = context.getMetricGroup().counter(ERROR_COUNT);
        retryCounter = context.getMetricGroup().counter(RETRY_COUNT);
    }

    protected OpenAICompletions createOpenAICompletions() {
        return new OpenAICompletions();
    }

    public String eval(String prompt, String modelName) {
        return eval(prompt, modelName, null, null, null);
    }

    public String eval(String prompt, String modelName, Integer maxOutputTokens) {
        return eval(prompt, modelName, maxOutputTokens, null, null);
    }

    public String eval(String prompt, String modelName, Integer maxOutputTokens, Double temperature) {
        return eval(prompt, modelName, maxOutputTokens, temperature, null);
    }

    public String eval(String prompt, String modelName, Integer maxOutputTokens, Double temperature, Double topP) {
        callCounter.inc();

        long start = System.nanoTime();

        String ret = executeWithRetry(
                () -> openAICompletions.callCompletions(prompt, modelName, false, maxOutputTokens, temperature, topP),
                () -> errorCounter.inc(),
                () -> retryCounter.inc()
        );

        long elapsedTime = System.nanoTime() - start;
        latencyTracker.recordLatency(TimeUnit.NANOSECONDS.toMillis(elapsedTime));

        return ret;
    }
}
