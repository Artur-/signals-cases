package com.example.usecase08;

public record FormData(String firstName, String lastName, String email,
        String companyName, String companySize, String industry,
        Plan selectedPlan) {
}
