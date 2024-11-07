package com.datasqrl.openai;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static com.datasqrl.openai.OpenAIUtil.API_KEY;
import static com.datasqrl.openai.OpenAIUtil.COMPLETIONS_API;

public class OpenAICompletions {

    private static final double TEMPERATURE_DEFAULT = 1.0;
    private static final double TOP_P_DEFAULT = 1.0;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient;

    public OpenAICompletions() {
        httpClient = HttpClient.newHttpClient();
    }

    public OpenAICompletions(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String callCompletions(String prompt, String modelName, Boolean requireJsonOutput, Integer maxOutputTokens, Double temperature, Double topP) throws IOException, InterruptedException {
        // Create the request body JSON
        ObjectNode requestBody = createRequestBody(prompt, modelName, requireJsonOutput, maxOutputTokens, temperature, topP);

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(COMPLETIONS_API))
                .header("Authorization", "Bearer " + System.getenv(API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle the response
        if (response.statusCode() == 200) {
            return extractContent(response.body());
        } else {
            throw new IOException("Failed to get completion: HTTP status code " + response.statusCode() + " Message: " + response.body());
        }
    }

    private ObjectNode createRequestBody(String prompt, String modelName, Boolean requireJsonOutput, Integer maxOutputTokens, Double temperature, Double topP) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", modelName);

        // Create the messages array as required by the chat completions endpoint
        ArrayNode messagesArray = objectMapper.createArrayNode();

        if (requireJsonOutput) {
            // when the model supports JSON output, both setting is needed otherwise the API call will fail
            requestBody.putObject("response_format").put("type", "json_object");
            messagesArray.add(createMessage("system", "You are a helpful assistant designed to output minified JSON."));
        }

        messagesArray.add(createMessage("user", prompt));

        requestBody.set("messages", messagesArray);
        requestBody.put("temperature", temperature != null ? temperature : TEMPERATURE_DEFAULT);
        requestBody.put("top_p", topP != null ? topP : TOP_P_DEFAULT);
        requestBody.put("n", 1); // Number of completions to generate

        if (maxOutputTokens != null) {
            requestBody.put("max_tokens", maxOutputTokens);
        }

        return requestBody;
    }

    private String extractContent(String jsonResponse) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        // Extract the content from the first choice
        return jsonNode.get("choices").get(0)
                .get("message").get("content").asText().trim();
    }

    private static ObjectNode createMessage(String role, String prompt) {
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", role);
        userMessage.put("content", prompt);
        return userMessage;
    }
}