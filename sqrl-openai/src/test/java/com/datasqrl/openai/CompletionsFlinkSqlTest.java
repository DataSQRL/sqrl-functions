package com.datasqrl.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import static org.junit.jupiter.api.Assertions.fail;

public class CompletionsFlinkSqlTest {

    private StreamTableEnvironment tEnv;
    private completions function;

    @BeforeEach
    public void setUp() {
        // Set up Flink Stream and Table environments
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        tEnv = StreamTableEnvironment.create(env, settings);

        // Create an instance of the function to register with the environment
        function = new completions();

        // Register the function in the Flink SQL environment
        tEnv.createTemporarySystemFunction("completions", function);
    }

    @Test
    public void testCompletionsFunctionWithSQL() {
        // Use static mocking for the callCompletions method in OpenAICompletions
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            // Define the mocked response for the static method call
            String expectedResponse = "{\"result\": \"test_completion_output\"}";
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.eq(false),
                    Mockito.anyInt(),
                    Mockito.anyDouble(),
                    Mockito.anyDouble()
            )).thenReturn(expectedResponse);

            // Execute SQL that uses the completions function
            TableResult result = tEnv.executeSql(
                    "SELECT completions('Test prompt.', 'gpt-4o', 100, 0.7, 0.9) AS completion_result"
            );

            // Collect and verify the result
            result.collect().forEachRemaining(row -> {
                String jsonOutput = row.getField("completion_result").toString();
                assertEquals(expectedResponse, jsonOutput);
            });
        }
    }

    @Test
    public void testCompletionsFunctionWithSQLAndDefaults() {
        // Use static mocking for the callCompletions method in OpenAICompletions
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            // Define the mocked response for the static method call
            String expectedResponse = "{\"result\": \"test_completion_output\"}";
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.eq(false),
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.any()
            )).thenReturn(expectedResponse);

            // Execute SQL that uses the completions function with default values
            TableResult result = tEnv.executeSql(
                    "SELECT completions('Test prompt.', 'gpt-4o') AS completion_result"
            );

            // Collect and verify the result
            result.collect().forEachRemaining(row -> {
                String jsonOutput = row.getField("completion_result").toString();
                assertEquals(expectedResponse, jsonOutput);
            });
        }
    }

    /**
     * This test demonstrates how to use the completions function with a real API call.
     * The test requires a valid OPENAI_API_KEY to be set as an environment variable.
     */
    @Test
    @Disabled
    public void testCompletionsFunctionWithSQLUsingRealApiCall() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Execute SQL that uses the completions function
        TableResult result = tEnv.executeSql(
                "SELECT completions('Reply this exactly: Hello.', 'gpt-4o', 50, 0.7, 0.9) AS completion_result"
        );

        // Collect and verify the result
        result.collect().forEachRemaining(row -> {
            String response = row.getField("completion_result").toString();
            try {
                objectMapper.readTree(response);
                fail("Should fail because the response should not be JSON.");
            } catch (JsonProcessingException e) {
                // Expected exception
            }

            assertEquals("Hello.", response);
        });
    }
}
