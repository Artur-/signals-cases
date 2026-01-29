package com.example.usecase22;

/**
 * Simple DTO class for the inbox list to demonstrate complex object data
 */
public class ServiceHealth {

    private Status status;

    private String city;

    private int input;

    private int output;

    public enum Status {
        EXCELLENT, OK, FAILING
    }

    public ServiceHealth(Status status, String city, int input, int output) {
        this.status = status;
        this.city = city;
        this.input = input;
        this.output = output;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCity() {
        return city;
    }

    public int getInput() {
        return input;
    }

    public void setInput(int input) {
        this.input = input;
    }

    public int getOutput() {
        return output;
    }

    public void setOutput(int output) {
        this.output = output;
    }

}
