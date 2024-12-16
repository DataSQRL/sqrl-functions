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

    @Mock
    private FunctionContext functionContext;

    @Mock
    private MetricGroup metricGroup;

    @Mock
    private Counter callCounter;

    @Mock
    private Counter errorCounter;

    private vector_embedd function;

    @BeforeEach
    void setUp() throws Exception {
        final String functionName = vector_embedd.class.getSimpleName();

        when(functionContext.getMetricGroup()).thenReturn(metricGroup);
        when(metricGroup.counter(eq(format(CALL_COUNT, functionName)))).thenReturn(callCounter);
        when(metricGroup.counter(eq(format(ERROR_COUNT, functionName)))).thenReturn(errorCounter);

        function = new vector_embedd() {
            @Override
            protected OpenAIEmbeddings createOpenAIEmbeddings() {
                return openAIEmbeddings;
            }
        };

        function.open(functionContext);
    }

    @Test
    void testEvalSuccessfulEmbedding() throws IOException, InterruptedException {
        // Mock response data
        String mockResponse = "{\"data\": [{\"embedding\": [0.1, 0.2, 0.3]}]}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockResponse);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        // Execute function
        CompletableFuture<double[]> future = new CompletableFuture<>();
        function.eval(future, "some text", "model-name");

        double[] result = future.join();

        verify(callCounter, times(1)).inc();
        verify(errorCounter, never()).inc();

        // Verify the result
        assertArrayEquals(new double[]{0.1, 0.2, 0.3}, result);
    }

    @Test
    void testEvalErrorHandling() throws IOException, InterruptedException {
        IOException exception = new IOException("Test Exception");

        // Mock the HttpClient to throw an IOException for retries
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(exception);

        // Attempt to call vectorEmbedd, expecting retries
        CompletableFuture<double[]> future = new CompletableFuture<>();
        function.eval(future, "some text", "model-name");

        try {
            future.join();
            fail("Expected an exception to be thrown");
        } catch (Exception e) {
            // expected
            assertEquals(exception, e.getCause());
        }

        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        verify(callCounter, times(1)).inc();
        verify(errorCounter, times(1)).inc();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTestArguments")
    void testEvalWhenInputIsInvalid(String prompt, String modelName) throws IOException, InterruptedException {
        CompletableFuture<double[]> future = new CompletableFuture<>();
        function.eval(future, prompt, modelName);

        double[] result = future.join();

        assertNull(result);

        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    private static Stream<Arguments> provideInvalidTestArguments() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of(null, "")
        );
    }
}