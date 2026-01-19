package com.example.usecase19;

import com.example.usecase14.LoadingState;

public record DataItem(
    String id,
    String name,
    LoadingState.State state,
    String data,
    String error,
    int simulatedDelayMs
) {}
