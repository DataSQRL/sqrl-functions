package com.datasqrl.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.datasqrl.openai.OpenAIUtil.API_KEY;
import static com.datasqrl.openai.OpenAIUtil.COMPLETIONS_API;

public class OpenAICompletions {

    private static final double TEMPERATURE_DEFAULT = 1.0;
    private static final double TOP_P_DEFAULT = 1.0;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String callCompletions(String prompt, String modelName, Boolean requireJsonOutput, Integer maxOutputTokens, Double temperature, Double topP) throws IOException {
        // Create the request body JSON
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", modelName);

        // Create the messages array as required by the chat completions endpoint
        ArrayNode messagesArray = objectMapper.createArrayNode();

        if (requireJsonOutput) {
            // when the model supports JSON output, both setting is needed otherwise the API call will fail
            requestBody.putObject("response_format").put("type", "json_object");
            messagesArray.add(createMessage("system", "You are a helpful assistant designed to output JSON."));
        }

        messagesArray.add(createMessage("user", prompt));

        requestBody.set("messages", messagesArray);
        requestBody.put("temperature", temperature != null ? temperature : TEMPERATURE_DEFAULT);
        requestBody.put("top_p", topP != null ? topP : TOP_P_DEFAULT);
        requestBody.put("n", 1); // Number of completions to generate

        if (maxOutputTokens != null) {
            requestBody.put("max_tokens", maxOutputTokens);
        }

        // Create an HTTP connection
        HttpURLConnection connection = (HttpURLConnection) new URL(COMPLETIONS_API).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + System.getenv(API_KEY));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send the request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read the response
        int statusCode = connection.getResponseCode();
        if (statusCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse JSON response
                JsonNode jsonResponse = objectMapper.readTree(response.toString());

                // Extract the content from the first choice
                String content = jsonResponse.get("choices").get(0)
                        .get("message").get("content").asText().trim();

                // Assuming the completion is valid JSON, parse it into a JsonNode
                return content;
            }
        } else {
            throw new IOException("Failed to get completion: HTTP status code " + statusCode + " Message: " + connection.getResponseMessage());
        }
    }

    private static ObjectNode createMessage(String role, String prompt) {
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", role);
        userMessage.put("content", prompt);
        return userMessage;
    }
}