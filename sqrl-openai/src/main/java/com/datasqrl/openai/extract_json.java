package com.datasqrl.openai;

import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

@AutoService(ScalarFunction.class)
public class extract_json extends ScalarFunction {

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

    public String eval(String prompt, String modelName) {
        return eval(prompt, modelName, null, null);
    }

    public String eval(String prompt, String modelName, Double temperature) {
        return eval(prompt, modelName, temperature, null);
    }

    public String eval(String prompt, String modelName, Double temperature, Double topP) {
        if (prompt == null || modelName == null) return null;

        return executor.execute(
                () -> openAICompletions.callCompletions(prompt, modelName, true, null, temperature, topP)
        );
    }
}
