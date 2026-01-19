package com.example.usecase18;

import java.time.Instant;

public record ChatMessageData(String role, String content, Instant timestamp) {
}
