package com.datasqrl.openai;

import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.ScalarFunction;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

@AutoService(ScalarFunction.class)
public class vector_embedd extends ScalarFunction {

    public double[] eval(String text, String modelName) {
        return eval(text, modelName, 0);
    }

    public double[] eval(String text, String modelName, int maxRetries) {
        return executeWithRetry(
                () -> OpenAIEmbeddings.vectorEmbedd(text, modelName),
                maxRetries
        );
    }
}
