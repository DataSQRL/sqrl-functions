package com.datasqrl.pytorch;

import com.google.auto.service.AutoService;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(ScalarFunction.class)
public class text_embedding extends ScalarFunction {

    private static final Logger logger = LoggerFactory.getLogger(text_embedding.class);

    private EmbeddingsService embeddingsService;

    @Override
    public void open(FunctionContext context) throws Exception {
        embeddingsService = new EmbeddingsService();
    }

    public float[] eval(String text, String modelName) {
        if (text == null || modelName == null) {
            return null;
        }

        try {
            return embeddingsService.embedd(text, modelName);
        } catch (Exception e) {
            logger.error("Error occurred while embedding text", e);
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        embeddingsService.close();
    }
}
