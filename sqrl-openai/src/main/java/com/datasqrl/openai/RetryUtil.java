package com.datasqrl.openai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Callable;

public class RetryUtil {

    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);

    private static final String MAX_RETRIES = "FUNCTION_MAX_RETRIES";

    public static <T> T executeWithRetry(Callable<T> task) {
        int maxRetries = Optional.ofNullable(System.getenv(MAX_RETRIES))
                .map(Integer::parseInt)
                .orElse(3);
        return executeWithRetry(task, maxRetries);
    }

    public static <T> T executeWithRetry(Callable<T> task, int maxRetries) {
        int attempts = 0;

        while (true) {
            try {
                return task.call(); // Attempt to execute the task
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxRetries) {
                    logger.error("Error occurred after {} attempts. Returning null.", maxRetries, e);
                    return null; // Return null if retries are exhausted
                }
                logger.warn("Attempt {} failed. Retrying...", attempts, e);
            }
        }
    }
}
