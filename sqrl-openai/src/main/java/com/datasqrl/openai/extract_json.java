package com.datasqrl.openai;

import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.ScalarFunction;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

@AutoService(ScalarFunction.class)
public class extract_json extends ScalarFunction {

    public String eval(String prompt, String modelName) {
        return eval(prompt, modelName, null, null);
    }

    public String eval(String prompt, String modelName, Double temperature) {
        return eval(prompt, modelName, temperature, null);
    }

    public String eval(String prompt, String modelName, Double temperature, Double topP) {
        return eval(prompt, modelName, temperature, topP, 0);
    }

    public String eval(String prompt, String modelName, Double temperature, Double topP, int maxRetries) {
        return executeWithRetry(
                () -> OpenAICompletions.callCompletions(prompt, modelName, true, null, temperature, topP),
                maxRetries
        );
    }
}
