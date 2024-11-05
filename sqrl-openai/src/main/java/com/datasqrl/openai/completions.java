package com.datasqrl.openai;

import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import static com.datasqrl.openai.RetryUtil.executeWithRetry;

@AutoService(ScalarFunction.class)
public class completions extends ScalarFunction {

    private OpenAICompletions openAICompletions;

    @Override
    public void open(FunctionContext context) throws Exception {
        this.openAICompletions = createOpenAICompletions();
    }

    protected OpenAICompletions createOpenAICompletions() {
        return new OpenAICompletions();
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
        return executeWithRetry(
                () -> openAICompletions.callCompletions(prompt, modelName, false, maxOutputTokens, temperature, topP)
        );
    }
}
