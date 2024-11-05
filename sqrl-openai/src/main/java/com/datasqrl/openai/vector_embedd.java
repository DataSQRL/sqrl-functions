package com.datasqrl.openai;

import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

@AutoService(ScalarFunction.class)
public class vector_embedd extends ScalarFunction {

    private OpenAIEmbeddings openAIEmbeddings;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAIEmbeddings = createOpenAIEmbeddings();
    }

    protected OpenAIEmbeddings createOpenAIEmbeddings() {
        return new OpenAIEmbeddings();
    }

    public double[] eval(String text, String modelName) {
        return executeWithRetry(
                () -> openAIEmbeddings.vectorEmbedd(text, modelName)
        );
    }
}
