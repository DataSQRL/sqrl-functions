package com.datasqrl.openai;

import com.datasqrl.openai.util.FunctionMetricTracker;
import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.AsyncScalarFunction;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.concurrent.CompletableFuture;

@AutoService(ScalarFunction.class)
public class completions extends AsyncScalarFunction {

    private transient OpenAICompletions openAICompletions;
    private transient FunctionExecutor executor;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAICompletions = createOpenAICompletions();
        this.executor = new FunctionExecutor(context, completions.class.getSimpleName());
    }

    protected OpenAICompletions createOpenAICompletions() {
        return new OpenAICompletions();
    }

    protected FunctionMetricTracker createMetricTracker(FunctionContext context, String functionName) {
        return new FunctionMetricTracker(context, functionName);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName) {
        eval(result, prompt, modelName, null, null, null);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName, Integer maxOutputTokens) {
        eval(result, prompt, modelName, maxOutputTokens, null, null);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName, Integer maxOutputTokens, Double temperature) {
        eval(result, prompt, modelName, maxOutputTokens, temperature, null);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName, Integer maxOutputTokens, Double temperature, Double topP) {
        executor.executeAsync(() -> openAICompletions.callCompletions(prompt, modelName, false, null, maxOutputTokens, temperature, topP))
                .thenAccept(result::complete)
                .exceptionally(ex -> { result.completeExceptionally(ex); return null; });
    }
}
