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
        eval(result, prompt, modelName, null);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName, Double temperature) {
        eval(result, prompt, modelName, temperature, null);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName, Double temperature, Double topP) {
        eval(result, prompt, modelName, temperature, topP, null);
    }

    public void eval(CompletableFuture<String> result, String prompt, String modelName, Double temperature, Double topP, String jsonSchema) {
        final CompletionsRequest request = new CompletionsRequest.CompletionsRequestBuilder()
                .prompt(prompt)
                .modelName(modelName)
                .requireJsonOutput(true)
                .jsonSchema(jsonSchema)
                .temperature(temperature)
                .topP(topP)
                .build();

        executeRequest(result, request);
    }

    private void executeRequest(CompletableFuture<String> result, CompletionsRequest request) {
        executor.executeAsync(() -> openAICompletions.callCompletions(request))
                .thenAccept(result::complete)
                .exceptionally(ex -> { result.completeExceptionally(ex); return null; });
    }
}
