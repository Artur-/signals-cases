package com.example.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for loading data asynchronously with simulated delays.
 *
 * Demonstrates parallel async loading where each data item loads independently
 * with its own delay and error simulation.
 */
@Service
public class DataLoadingService {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Loads data asynchronously with a simulated delay.
     *
     * The @Async annotation causes Spring to execute this method in a separate thread
     * from a configured thread pool, enabling true parallel execution.
     *
     * @param id Unique identifier for the data item
     * @param delayMs Simulated loading delay in milliseconds
     * @param shouldSimulateErrors If true, has ~30% chance to fail
     * @return CompletableFuture containing the loaded data
     */
    @Async
    public CompletableFuture<String> loadDataAsync(String id, int delayMs, boolean shouldSimulateErrors) {
        try {
            // Simulate data loading delay (e.g., database query, API call)
            Thread.sleep(delayMs);

            // Randomly fail if error simulation is enabled (30% chance)
            if (shouldSimulateErrors && RANDOM.nextDouble() < 0.3) {
                throw new RuntimeException("Failed to load data from server");
            }

            // Generate mock data with timestamp
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String data = String.format(
                "Data loaded successfully!\n" +
                "Time: %s\n" +
                "Duration: %dms\n" +
                "Sample values: %d, %d, %d",
                timestamp,
                delayMs,
                RANDOM.nextInt(100),
                RANDOM.nextInt(100),
                RANDOM.nextInt(100)
            );

            return CompletableFuture.completedFuture(data);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Data loading was interrupted");
        }
    }
}
