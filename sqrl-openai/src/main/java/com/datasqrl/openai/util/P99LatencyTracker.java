package com.datasqrl.openai.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class P99LatencyTracker {
    private final List<Long> latencies; // Stores the recorded latencies
    private final int maxSize; // Maximum size of the sliding window

    public P99LatencyTracker(int maxSize) {
        this.maxSize = maxSize;
        this.latencies = new ArrayList<>();
    }

    public void recordLatency(long latencyMs) {
        latencies.add(latencyMs);

        // If the size exceeds maxSize, remove the oldest entry
        if (latencies.size() > maxSize) {
            latencies.remove(0); // Remove the oldest entry
        }
    }

    public Long getP99Latency() {
        if (latencies.isEmpty()) {
            return 0L;
        }

        // Sort the latencies to find the P99 value
        Collections.sort(latencies);
        int index = (int) Math.ceil(0.99 * latencies.size()) - 1; // P99 index
        return latencies.get(index);
    }
}