package com.datasqrl.openai;

import com.datasqrl.openai.util.FunctionMetricTracker;
import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.concurrent.TimeUnit;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

@AutoService(ScalarFunction.class)
public class completions extends ScalarFunction {

    private OpenAICompletions openAICompletions;
    private FunctionMetricTracker metricTracker;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAICompletions = createOpenAICompletions();
        this.metricTracker = createMetricTracker(context, completions.class.getSimpleName());
    }

    protected OpenAICompletions createOpenAICompletions() {
        return new OpenAICompletions();
    }

    protected FunctionMetricTracker createMetricTracker(FunctionContext context, String functionName) {
        return new FunctionMetricTracker(context, functionName);
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
        metricTracker.increaseCallCount();

        long start = System.nanoTime();

        String ret = executeWithRetry(
                () -> openAICompletions.callCompletions(prompt, modelName, false, maxOutputTokens, temperature, topP),
                () -> metricTracker.increaseErrorCount(),
                () -> metricTracker.increaseRetryCount()
        );

        long elapsedTime = System.nanoTime() - start;
        metricTracker.recordLatency(TimeUnit.NANOSECONDS.toMillis(elapsedTime));

        return ret;
    }
}
