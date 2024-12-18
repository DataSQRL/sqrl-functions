package com.datasqrl.openai.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class P99LatencyTrackerTest {

    private P99LatencyTracker tracker;

    @BeforeEach
    public void setUp() {
        // Set up a new tracker with a max size of 100
        tracker = new P99LatencyTracker(100);
    }

    @Test
    public void testP99LatencyWithLessThan100Entries() {
        tracker.recordLatency(100);
        tracker.recordLatency(200);
        tracker.recordLatency(300);
        tracker.recordLatency(400);
        tracker.recordLatency(500);

        // The P99 latency should be the maximum value since we have less than 100 entries
        assertEquals(500, tracker.getP99Latency());
    }

    @Test
    public void testP99LatencyWithExactly100Entries() {
        for (int i = 1; i <= 100; i++) {
            tracker.recordLatency(i);
        }

        // The P99 latency should be the 99th largest value, which is 99 in this case
        assertEquals(99, tracker.getP99Latency());
    }

    @Test
    public void testP99LatencyWithMoreThan100Entries() {
        for (int i = 1; i <= 120; i++) {
            tracker.recordLatency(i);
        }

        // The P99 latency should reflect the highest 99% of the last 100 records
        assertEquals(119, tracker.getP99Latency());
    }

    @Test
    public void testP99LatencyWithEmptyTracker() {
        // When no latencies have been recorded, P99 should return 0
        assertEquals(0, tracker.getP99Latency());
    }

    @Test
    public void testSlidingWindowBehavior() {
        // Add 5 latencies
        tracker.recordLatency(100);
        tracker.recordLatency(200);
        tracker.recordLatency(300);
        tracker.recordLatency(400);
        tracker.recordLatency(500);

        // Initial P99 should be 500
        assertEquals(500, tracker.getP99Latency());

        // Now add more latencies to exceed the max size (e.g., 101)
        for (int i = 600; i <= 700; i++) {
            tracker.recordLatency(i);
        }

        // The oldest latencies (100-500) should have been removed,
        // Now the highest 99% of the new list should be considered
        assertEquals(699, tracker.getP99Latency());
    }
}