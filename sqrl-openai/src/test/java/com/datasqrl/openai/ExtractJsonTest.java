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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExtractJsonTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    @InjectMocks
    private OpenAICompletions openAICompletions;

    private extract_json function;

    @BeforeEach
    void setUp() throws Exception {
        function = new extract_json() {
            @Override
            public OpenAICompletions createOpenAICompletions() {
                return openAICompletions;
            }
        };
        function.open(null);
    }

    @Test
    void testEvalSuccessfulCompletion() throws IOException, InterruptedException {
        String responseBody = "{\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"content\": \"Hello.\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        String expectedResponse = "Hello.";

        // Configure the mock to return a successful response
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(responseBody);

        String result = function.eval("prompt", "model", 0.1, 0.9);

        assertEquals(expectedResponse, result);
    }

    @Test
    void testEvalWithDefaults() throws IOException, InterruptedException {
        String responseBody = "{\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"content\": \"Hello.\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        String expectedResponse = "Hello.";

        // Configure the mock to return a successful response with defaults
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(responseBody);

        String result = function.eval("prompt", "model");

        assertEquals(expectedResponse, result);
    }

    @Test
    void testEvalErrorHandling() throws IOException, InterruptedException {
        // Configure the mock to throw an IOException, simulating repeated failures
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Test Exception"));

        String result = function.eval("prompt", "model", 0.7, 0.9);

        // Verify that the send method was attempted 3 times due to retry logic
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        assertNull(result);
    }
}