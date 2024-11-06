package com.datasqrl.openai;

import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.table.functions.FunctionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.datasqrl.openai.completions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CompletionsTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    @Mock
    private FunctionContext functionContext;

    @Mock
    private MetricGroup metricGroup;

    @Mock
    private Counter callCounter;

    @Mock
    private Counter errorCounter;

    @Mock
    private Counter retryCounter;

    @InjectMocks
    private OpenAICompletions openAICompletions;

    private completions function;

    @BeforeEach
    void setUp() throws Exception {
        when(functionContext.getMetricGroup()).thenReturn(metricGroup);
        when(metricGroup.counter(eq(CALL_COUNT))).thenReturn(callCounter);
        when(metricGroup.counter(eq(ERROR_COUNT))).thenReturn(errorCounter);
        when(metricGroup.counter(eq(RETRY_COUNT))).thenReturn(retryCounter);

        function = new completions() {
            @Override
            public OpenAICompletions createOpenAICompletions() {
                return openAICompletions;
            }
        };
        function.open(functionContext);
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

        // Configure mock HttpClient to return a successful response
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(responseBody);

        String result = function.eval("prompt", "model", 100, 0.1, 0.9);

        verify(callCounter, times(1)).inc();
        verify(errorCounter, never()).inc();
        verify(retryCounter, never()).inc();

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

        // Configure mock HttpClient to return a successful response
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(responseBody);

        String result = function.eval("prompt", "model");

        verify(callCounter, times(1)).inc();
        verify(errorCounter, never()).inc();
        verify(retryCounter, never()).inc();

        assertEquals(expectedResponse, result);
    }

    @Test
    void testEvalErrorHandling() throws IOException, InterruptedException {
        // Configure the mock to throw an IOException, simulating repeated failures
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Test Exception"));

        String result = function.eval("prompt", "model", 100, 0.1, 0.9);

        // Verify that the send method was attempted 3 times
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        verify(callCounter, times(1)).inc();
        verify(errorCounter, times(1)).inc();
        verify(retryCounter, times(2)).inc();

        assertNull(result);
    }
}