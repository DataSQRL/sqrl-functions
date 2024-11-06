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
public class vector_embedd extends ScalarFunction {

    public static final String P99_METRIC = "com.datasqrl.openai.vector_embedd.p99";
    public static final String CALL_COUNT = "com.datasqrl.openai.vector_embedd.callCount";
    public static final String ERROR_COUNT = "com.datasqrl.openai.vector_embedd.errorCount";
    public static final String RETRY_COUNT = "com.datasqrl.openai.vector_embedd.retryCount";

    private OpenAIEmbeddings openAIEmbeddings;

    private transient P99LatencyTracker latencyTracker;
    private transient Counter callCount;
    private transient Counter errorCount;
    private transient Counter retryCount;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAIEmbeddings = createOpenAIEmbeddings();
        this.latencyTracker = new P99LatencyTracker(100);
        context.getMetricGroup()
                .gauge(P99_METRIC, (Gauge<Long>) () -> latencyTracker.getP99Latency());
        callCount = context.getMetricGroup().counter(CALL_COUNT);
        errorCount = context.getMetricGroup().counter(ERROR_COUNT);
        retryCount = context.getMetricGroup().counter(RETRY_COUNT);
    }

    protected OpenAIEmbeddings createOpenAIEmbeddings() {
        return new OpenAIEmbeddings();
    }

    public double[] eval(String text, String modelName) {
        callCount.inc();

        long start = System.nanoTime();

        double[] ret = executeWithRetry(
                () -> openAIEmbeddings.vectorEmbedd(text, modelName),
                () -> errorCount.inc(),
                () -> retryCount.inc()
        );

        long elapsedTime = System.nanoTime() - start;
        latencyTracker.recordLatency(TimeUnit.NANOSECONDS.toMillis(elapsedTime));

        return ret;
    }
}
