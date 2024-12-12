package com.datasqrl.openai;

import com.datasqrl.openai.util.FunctionMetricTracker;
import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.AsyncScalarFunction;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.concurrent.CompletableFuture;

@AutoService(ScalarFunction.class)
public class vector_embedd extends AsyncScalarFunction {

    private transient OpenAIEmbeddings openAIEmbeddings;
    private transient FunctionExecutor executor;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAIEmbeddings = createOpenAIEmbeddings();
        this.executor = new FunctionExecutor(context, vector_embedd.class.getSimpleName());
    }

    protected OpenAIEmbeddings createOpenAIEmbeddings() {
        return new OpenAIEmbeddings();
    }

    protected FunctionMetricTracker createMetricTracker(FunctionContext context, String functionName) {
        return new FunctionMetricTracker(context, functionName);
    }

    public void eval(CompletableFuture<double[]> result, String text, String modelName) {
        executor.executeAsync(() -> openAIEmbeddings.vectorEmbedd(text, modelName))
                .thenAccept(result::complete)
                .exceptionally(ex -> { result.completeExceptionally(ex); return null; });
    }
}
