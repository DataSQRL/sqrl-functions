package com.datasqrl.openai;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class ExtractJsonTest {

    @Test
    void testEvalSuccessfulCompletion() {
        // Mock the static method call
        String expectedResponse = "{\"key\": \"value\"}";

        // Prepare for static mocking
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(anyString(), anyString(), eq(true),
                            any(), anyDouble(), anyDouble()))
                    .thenReturn(expectedResponse);

            extract_json function = new extract_json();
            String result = function.eval("prompt", "model", 0.7, 0.9);

            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void testEvalWithDefaults() {
        String expectedResponse = "{\"key\": \"default\"}";

        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(anyString(), anyString(), eq(true), any(), isNull(), isNull()))
                    .thenReturn(expectedResponse);

            extract_json function = new extract_json();
            String result = function.eval("prompt", "model"); // Calling without optional params

            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void testEvalErrorHandling() {
        // Mock the static method to throw IOException
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                            anyString(),
                            anyString(),
                            eq(true),
                            any(),
                            anyDouble(),
                            anyDouble()))
                    .thenThrow(new IOException("Test Exception"));

            extract_json function = new extract_json();
            String result = function.eval("prompt", "model", 0.7, 0.9);

            // Verify that the callCompletions method was called only once
            mockedCompletions.verify(() -> OpenAICompletions.callCompletions(
                    anyString(),
                    anyString(),
                    eq(true),
                    any(),
                    anyDouble(),
                    anyDouble()
            ), times(1));

            assertNull(result);
        }
    }

    @Test
    public void testEvalRetriesOnFailure() {
        // Mock the static method in OpenAICompletions
        try (MockedStatic<OpenAICompletions> mockedCompletions = Mockito.mockStatic(OpenAICompletions.class)) {
            // Set up the mock to return null each time, simulating a failure
            mockedCompletions.when(() -> OpenAICompletions.callCompletions(
                            anyString(),
                            anyString(),
                            eq(true),
                            any(),
                            anyDouble(),
                            anyDouble()))
                    .thenThrow(new IOException("Test Exception"));

            // Execute the function with retries
            extract_json function = new extract_json();
            String result = function.eval("prompt", "model", 0.7, 0.9, 3);

            // Verify that the callCompletions method was attempted 3 times
            mockedCompletions.verify(() -> OpenAICompletions.callCompletions(
                    anyString(),
                    anyString(),
                    eq(true),
                    any(),
                    anyDouble(),
                    anyDouble()
            ), times(3));

            // Ensure that result is null after exhausting retries
            assertNull(result);
        }
    }
}