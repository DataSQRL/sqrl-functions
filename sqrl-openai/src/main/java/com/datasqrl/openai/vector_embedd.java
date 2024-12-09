package com.datasqrl.openai;

import com.datasqrl.openai.util.FunctionMetricTracker;
import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

@AutoService(ScalarFunction.class)
public class vector_embedd extends ScalarFunction {

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

    public double[] eval(String text, String modelName) {
        if (text == null || modelName == null) return null;

        return executor.execute(
                () -> openAIEmbeddings.vectorEmbedd(text, modelName)
        );
    }
}
