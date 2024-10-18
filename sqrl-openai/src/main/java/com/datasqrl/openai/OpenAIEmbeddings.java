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
import static com.datasqrl.openai.OpenAIUtil.EMBEDDING_API;

public class OpenAIEmbeddings {

    private static final int TOKEN_LIMIT = 8192;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static double[] vectorEmbedd(String text, String modelName) throws IOException {
        return vectorEmbedd(text, modelName, TOKEN_LIMIT);
    }

    public static double[] vectorEmbedd(String text, String modelName, int tokenLimit) throws IOException {
        // Truncate text to fit the maximum token limit
        text = truncateText(text, tokenLimit);

        // Create the request body JSON
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("input", text);
        requestBody.put("model", modelName);

        // Create an HTTP connection
        HttpURLConnection connection = (HttpURLConnection) new URL(EMBEDDING_API).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + System.getenv(API_KEY));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send the request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = objectMapper.writeValueAsBytes(requestBody);
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
                ArrayNode embeddingArray = (ArrayNode) jsonResponse.get("data").get(0).get("embedding");

                // Convert JSON array to a double array (embedding vector)
                double[] embeddingVector = new double[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    embeddingVector[i] = embeddingArray.get(i).asDouble();
                }
                return embeddingVector;
            }
        } else {
            throw new IOException("Failed to get embedding: HTTP status code " + statusCode);
        }
    }

    // Method to truncate the text if it exceeds the token limit (adjust as needed)
    private static String truncateText(String text, int maxTokens) {
        if (text.length() > maxTokens) {
            // Simple truncation based on character length (you can improve this to token-based truncation)
            return text.substring(0, maxTokens);
        }
        return text;
    }
}