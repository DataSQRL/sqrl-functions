package com.datasqrl.openai;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class VectorEmbeddTest {

    @Test
    void testEvalSuccessfulEmbedding() {
        double[] expectedResponse = {0.1, 0.2, 0.3}; // Example embedding response

        try (MockedStatic<OpenAIEmbeddings> mockedEmbeddings = Mockito.mockStatic(OpenAIEmbeddings.class)) {
            mockedEmbeddings.when(() -> OpenAIEmbeddings.vectorEmbedd(
                            anyString(), anyString()))
                    .thenReturn(expectedResponse);

            vector_embedd function = new vector_embedd();
            double[] result = function.eval("some text", "model-name");

            assertArrayEquals(expectedResponse, result);
        }
    }

    @Test
    void testEvalErrorHandling() {
        // Mock the static method to throw IOException
        try (MockedStatic<OpenAIEmbeddings> mockedEmbeddings = Mockito.mockStatic(OpenAIEmbeddings.class)) {
            mockedEmbeddings.when(() -> OpenAIEmbeddings.vectorEmbedd(
                            anyString(),
                            anyString()))
                    .thenThrow(new IOException("Test Exception"));

            vector_embedd function = new vector_embedd();
            double[] result = function.eval("some text", "model-name");

            // Verify that the callCompletions method was called only once
            mockedEmbeddings.verify(() -> OpenAIEmbeddings.vectorEmbedd(
                    anyString(),
                    anyString()
            ), times(1));

            // Check that null is returned
            assertArrayEquals(null, result);
        }
    }

    @Test
    public void testEvalRetriesOnFailure() {
        // Mock the static method in OpenAICompletions
        try (MockedStatic<OpenAIEmbeddings> mockedEmbeddings = Mockito.mockStatic(OpenAIEmbeddings.class)) {
            // Set up the mock to return null each time, simulating a failure
            mockedEmbeddings.when(() -> OpenAIEmbeddings.vectorEmbedd(
                            anyString(),
                            anyString()))
                    .thenThrow(new IOException("Test Exception"));

            // Execute the function with retries
            vector_embedd function = new vector_embedd();
            double[] result = function.eval("some text", "model-name", 3);

            // Verify that the callCompletions method was attempted 3 times
            mockedEmbeddings.verify(() -> OpenAIEmbeddings.vectorEmbedd(
                    anyString(),
                    anyString()
            ), times(3));

            // Ensure that result is null after exhausting retries
            assertNull(result);
        }
    }
}