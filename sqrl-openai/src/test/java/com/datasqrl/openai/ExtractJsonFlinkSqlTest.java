package com.datasqrl.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtractJsonFlinkSqlTest {

    private StreamTableEnvironment tEnv;
    private extract_json function;

    @BeforeEach
    public void setUp() {
        // Set up Flink Stream and Table environments
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        tEnv = StreamTableEnvironment.create(env, settings);

        // Create an instance of the function to register with the environment
        function = new extract_json();

        // Register the function in the Flink SQL environment
        tEnv.createTemporarySystemFunction("extract_json", function);
    }

    @Test
    public void testExtractJsonFunctionWithSQL() {
        // Use static mocking for the callCompletions method in OpenAICompletions
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            // Define the mocked response for the static method call
            String expectedResponse = "{\"result\": \"test_output\"}";
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyBoolean(),
                    Mockito.any(),
                    Mockito.anyDouble(),
                    Mockito.anyDouble()
            )).thenReturn(expectedResponse);

            // Execute SQL that uses the extract_json function
            TableResult result = tEnv.executeSql(
                    "SELECT extract_json('Test prompt.', 'gpt-4o', 0.7, 0.9) AS json_result"
            );

            // Collect and verify the result
            result.collect().forEachRemaining(row -> {
                String jsonOutput = row.getField("json_result").toString();
                assertEquals(expectedResponse, jsonOutput);
            });
        }
    }

    @Test
    public void testExtractJsonFunctionWithSQLAndDefaults() {
        // Use static mocking for the callCompletions method in OpenAICompletions
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            // Define the mocked response for the static method call
            String expectedResponse = "{\"result\": \"test_output\"}";
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyBoolean(),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any()
            )).thenReturn(expectedResponse);

            // Execute SQL that uses the extract_json function
            TableResult result = tEnv.executeSql(
                    "SELECT extract_json('Test prompt.', 'gpt-4o') AS json_result"
            );

            // Collect and verify the result
            result.collect().forEachRemaining(row -> {
                String jsonOutput = row.getField("json_result").toString();
                assertEquals(expectedResponse, jsonOutput);
            });
        }
    }

    /**
     * This test demonstrates how to use the extract_json function with a real API call.
     * The test requires a valid OPENAI_API_KEY to be set as an environment variable.
     */
    @Test
    @Disabled
    public void testExtractJsonFunctionWithSQLUsingRealApiCall() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Execute SQL that uses the extract_json function
        TableResult result = tEnv.executeSql(
                "SELECT extract_json('Reply with a json object containing a response field with \"Test prompt received. <3\" value', 'gpt-4o', 0.7, 0.9) AS json_result"
        );

        // Collect and verify the result
        result.collect().forEachRemaining(row -> {
            String jsonOutput = row.getField("json_result").toString();
            JsonNode json;
            try {
                json = objectMapper.readTree(jsonOutput);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            assertEquals("Test prompt received. <3", json.get("response").asText());
        });
    }
}