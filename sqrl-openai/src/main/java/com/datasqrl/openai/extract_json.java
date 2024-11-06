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
public class extract_json extends ScalarFunction {

    public static final String P99_METRIC = "com.datasqrl.openai.extract_json.p99";
    public static final String CALL_COUNT = "com.datasqrl.openai.extract_json.callCount";
    public static final String ERROR_COUNT = "com.datasqrl.openai.extract_json.errorCount";
    public static final String RETRY_COUNT = "com.datasqrl.openai.extract_json.retryCount";

    private OpenAICompletions openAICompletions;

    private transient P99LatencyTracker latencyTracker;
    private transient Counter callCount;
    private transient Counter errorCount;
    private transient Counter retryCount;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAICompletions = createOpenAICompletions();
        this.latencyTracker = new P99LatencyTracker(100);
        context.getMetricGroup()
                .gauge(P99_METRIC, (Gauge<Long>) () -> latencyTracker.getP99Latency());
        callCount = context.getMetricGroup().counter(CALL_COUNT);
        errorCount = context.getMetricGroup().counter(ERROR_COUNT);
        retryCount = context.getMetricGroup().counter(RETRY_COUNT);
    }

    public OpenAICompletions createOpenAICompletions() {
        return new OpenAICompletions();
    }

    public String eval(String prompt, String modelName) {
        return eval(prompt, modelName, null, null);
    }

    public String eval(String prompt, String modelName, Double temperature) {
        return eval(prompt, modelName, temperature, null);
    }

    public String eval(String prompt, String modelName, Double temperature, Double topP) {
        callCount.inc();

        long start = System.nanoTime();

        String ret = executeWithRetry(
                () -> openAICompletions.callCompletions(prompt, modelName, true, null, temperature, topP),
                () -> errorCount.inc(),
                () -> retryCount.inc()
        );

        long elapsedTime = System.nanoTime() - start;
        latencyTracker.recordLatency(TimeUnit.NANOSECONDS.toMillis(elapsedTime));

        return ret;
    }
}
