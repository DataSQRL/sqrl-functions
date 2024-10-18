package com.datasqrl.openai;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class CompletionsTest {

    @Test
    void testEvalSuccessfulCompletion() {
        String expectedResponse = "{\"key\": \"completion_value\"}";

        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                            anyString(), anyString(), eq(false), anyInt(), anyDouble(), anyDouble()))
                    .thenReturn(expectedResponse);

            completions function = new completions();
            String result = function.eval("prompt", "model", 100, 0.7, 0.9);

            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void testEvalWithDefaults() {
        String expectedResponse = "{\"key\": \"completion_default\"}";

        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                            anyString(), anyString(), eq(false), isNull(), isNull(), isNull()))
                    .thenReturn(expectedResponse);

            completions function = new completions();
            String result = function.eval("prompt", "model");

            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void testEvalErrorHandling() {
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                            anyString(), anyString(), eq(false), anyInt(), anyDouble(), anyDouble()))
                    .thenThrow(new IOException("Test Exception"));

            completions function = new completions();
            String result = function.eval("prompt", "model", 100, 0.7, 0.9);

            // Verify that the callCompletions method was called only once
            mockedCompletions.verify(() -> OpenAICompletions.callCompletions(
                    anyString(),
                    anyString(),
                    eq(false),
                    anyInt(),
                    anyDouble(),
                    anyDouble()
            ), times(1));

            assertNull(result);
        }
    }

    @Test
    public void testCallCompletionsRetriesOnFailure() {
        // Mock the static method in OpenAICompletions
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            // Set up the mock to return null each time, simulating a failure
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                            anyString(),
                            anyString(),
                            eq(false),
                            anyInt(),
                            anyDouble(),
                            anyDouble()))
                    .thenThrow(new IOException("Test Exception"));

            // Execute the function with retries
            completions function = new completions();
            String result = function.eval("Test prompt.", "gpt-4o", 100, 0.7, 0.9, 3);

            // Verify that the callCompletions method was attempted 3 times
            mockedCompletions.verify(() -> OpenAICompletions.callCompletions(
                    anyString(),
                    anyString(),
                    eq(false),
                    anyInt(),
                    anyDouble(),
                    anyDouble()
            ), times(3));

            // Ensure that result is null after exhausting retries
            assertNull(result);
        }
    }
}
