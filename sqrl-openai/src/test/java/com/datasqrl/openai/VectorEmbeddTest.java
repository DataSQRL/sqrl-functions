package com.datasqrl.openai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VectorEmbeddTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private OpenAIEmbeddings openAIEmbeddings;

    private vector_embedd function;

    @BeforeEach
    public void setUp() throws Exception {
        function = new vector_embedd() {
            @Override
            protected OpenAIEmbeddings createOpenAIEmbeddings() {
                return openAIEmbeddings;
            }
        };
        function.open(null);
    }

    @Test
    void testEvalSuccessfulEmbedding() throws IOException, InterruptedException {
        // Mock response data
        String mockResponse = "{\"data\": [{\"embedding\": [0.1, 0.2, 0.3]}]}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockResponse);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        // Execute function
        double[] result = function.eval("some text", "model-name");

        // Verify the result
        assertArrayEquals(new double[]{0.1, 0.2, 0.3}, result);
    }

    @Test
    void testEvalErrorHandling() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Test Exception"));

        // Execute function with exception handling
        double[] result = function.eval("some text", "model-name");

        // Verify that result is null if IOException is thrown
        assertNull(result);
    }

    @Test
    void testEvalRetriesOnFailure() throws IOException, InterruptedException {
        // Mock the HttpClient to throw an IOException for retries
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Test Exception"));

        // Attempt to call vectorEmbedd, expecting retries
        double[] result = function.eval("some text", "model-name");

        // Verify that HttpClient's send method was called 3 times due to retries
        verify(httpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        // Ensure that result is null after exhausting retries
        assertNull(result);
    }
}