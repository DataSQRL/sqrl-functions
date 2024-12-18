package com.datasqrl.openai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompletionsRequest {
    private final String prompt;
    private final String modelName;
    private final boolean requireJsonOutput;
    private final String jsonSchema;
    private final Integer maxOutputTokens;
    private final Double temperature;
    private final Double topP;
}
