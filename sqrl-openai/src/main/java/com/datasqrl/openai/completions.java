package com.datasqrl.openai;

import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.ScalarFunction;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

@AutoService(ScalarFunction.class)
public class completions extends ScalarFunction {

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
        return eval(prompt, modelName, maxOutputTokens, temperature, topP, 0);
    }

    public String eval(String prompt, String modelName, Integer maxOutputTokens, Double temperature, Double topP, int maxRetries) {
        return executeWithRetry(
                () -> OpenAICompletions.callCompletions(prompt, modelName, false, maxOutputTokens, temperature, topP),
                maxRetries
        );
    }
}
