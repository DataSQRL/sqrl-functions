package com.datasqrl.openai;

import com.datasqrl.openai.util.FunctionMetricTracker;
import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.concurrent.TimeUnit;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

@AutoService(ScalarFunction.class)
public class vector_embedd extends ScalarFunction {

    private transient OpenAIEmbeddings openAIEmbeddings;
    private transient FunctionMetricTracker metricTracker;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAIEmbeddings = createOpenAIEmbeddings();
        this.metricTracker = createMetricTracker(context, vector_embedd.class.getSimpleName());
    }

    protected OpenAIEmbeddings createOpenAIEmbeddings() {
        return new OpenAIEmbeddings();
    }

    protected FunctionMetricTracker createMetricTracker(FunctionContext context, String functionName) {
        return new FunctionMetricTracker(context, functionName);
    }

    public double[] eval(String text, String modelName) {
        if (text == null || modelName == null) return null;

        metricTracker.increaseCallCount();

        long start = System.nanoTime();

        double[] ret = executeWithRetry(
                () -> openAIEmbeddings.vectorEmbedd(text, modelName),
                () -> metricTracker.increaseErrorCount(),
                () -> metricTracker.increaseRetryCount()
        );

        long elapsedTime = System.nanoTime() - start;
        metricTracker.recordLatency(TimeUnit.NANOSECONDS.toMillis(elapsedTime));

        return ret;
    }
}
