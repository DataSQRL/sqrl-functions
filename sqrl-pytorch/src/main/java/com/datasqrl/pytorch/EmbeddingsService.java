package com.datasqrl.pytorch;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class EmbeddingsService {

    private ZooModel<String, float[]> model = null;

    public float[] embedd(String text, String modelName) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {

        if (model == null) {
            Criteria<String, float[]> criteria =
                    Criteria.builder()
                            .setTypes(String.class, float[].class)
                            .optModelName(modelName)
                            .optEngine("PyTorch")
                            .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                            .optProgress(new ProgressBar())
                            .build();

            model = criteria.loadModel();
        }

        try (Predictor<String, float[]> predictor = model.newPredictor()) {
            return predictor.predict(text);
        }
    }

    public void close() {
        if (model != null) {
            model.close();
        }
    }
}
