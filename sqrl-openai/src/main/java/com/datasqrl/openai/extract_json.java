package com.datasqrl.openai;

import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.AsyncScalarFunction;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.concurrent.CompletableFuture;

@AutoService(ScalarFunction.class)
public class extract_json extends AsyncScalarFunction {

    private transient OpenAICompletions openAICompletions;
    private transient FunctionExecutor executor;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAICompletions = createOpenAICompletions();
        this.executor = new FunctionExecutor(context, extract_json.class.getSimpleName());
    }

    protected OpenAICompletions createOpenAICompletions() {
        return new OpenAICompletions();
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName) {
        eval(result, prompt, modelName, null, null);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName, Double temperature) {
        eval(result, prompt, modelName, temperature, null);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName, Double temperature, Double topP) {
        executor.executeAsync(() -> openAICompletions.callCompletions(prompt, modelName, true, null, null, temperature, topP))
                .thenAccept(result::complete)
                .exceptionally(ex -> { result.completeExceptionally(ex); return null; });
    }
}
