package com.datasqrl.openai;

import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.table.functions.FunctionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.datasqrl.openai.util.FunctionMetricTracker.*;
import static java.lang.String.format;
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
        final String functionName = completions.class.getSimpleName();

        when(functionContext.getMetricGroup()).thenReturn(metricGroup);
        when(metricGroup.counter(eq(format(CALL_COUNT, functionName)))).thenReturn(callCounter);
        when(metricGroup.counter(eq(format(ERROR_COUNT, functionName)))).thenReturn(errorCounter);
        when(metricGroup.counter(eq(format(RETRY_COUNT, functionName)))).thenReturn(retryCounter);

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

        CompletableFuture<String> future = new CompletableFuture<>();
        function.eval(future, "prompt", "model", 100, 0.1, 0.9);

        String result = future.join();

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

        CompletableFuture<String> future = new CompletableFuture<>();
        function.eval(future, "prompt", "model");

        String result = future.join();

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

        CompletableFuture<String> future = new CompletableFuture<>();
        function.eval(future, "prompt", "model", 100, 0.1, 0.9);

        String result = future.join();

                // Verify that the send method was attempted 3 times
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        verify(callCounter, times(1)).inc();
        verify(errorCounter, times(1)).inc();
        verify(retryCounter, times(2)).inc();

        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTestArguments")
    void testEvalWhenInputIsInvalid(String prompt, String modelName) throws IOException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        function.eval(future, prompt, modelName);

        String result = future.join();

        assertNull(result);

        verify(mockHttpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    private static Stream<Arguments> provideInvalidTestArguments() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of(null, "")
        );
    }
}