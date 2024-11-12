package com.datasqrl.openai;

import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.table.functions.FunctionContext;
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

    @Mock
    private Counter retryCounter;

    private vector_embedd function;

    @BeforeEach
    void setUp() throws Exception {
        final String functionName = vector_embedd.class.getSimpleName();

        when(functionContext.getMetricGroup()).thenReturn(metricGroup);
        when(metricGroup.counter(eq(format(CALL_COUNT, functionName)))).thenReturn(callCounter);
        when(metricGroup.counter(eq(format(ERROR_COUNT, functionName)))).thenReturn(errorCounter);
        when(metricGroup.counter(eq(format(RETRY_COUNT, functionName)))).thenReturn(retryCounter);

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
        double[] result = function.eval("some text", "model-name");

        verify(callCounter, times(1)).inc();
        verify(errorCounter, never()).inc();
        verify(retryCounter, never()).inc();

        // Verify the result
        assertArrayEquals(new double[]{0.1, 0.2, 0.3}, result);
    }

    @Test
    void testEvalErrorHandling() throws IOException, InterruptedException {
        // Mock the HttpClient to throw an IOException for retries
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Test Exception"));

        // Attempt to call vectorEmbedd, expecting retries
        double[] result = function.eval("some text", "model-name");

        // Verify that HttpClient's send method was called 3 times due to retries
        verify(httpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        verify(callCounter, times(1)).inc();
        verify(errorCounter, times(1)).inc();
        verify(retryCounter,times(2)).inc();

        // Ensure that result is null after exhausting retries
        assertNull(result);
    }

    @Test
    void testEvalWhenInputIsInvalid() throws IOException, InterruptedException {
        assertNull(function.eval(null, null));
        assertNull(function.eval("", null));
        assertNull(function.eval(null, ""));

        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}