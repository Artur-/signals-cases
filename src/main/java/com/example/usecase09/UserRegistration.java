package com.example.usecase09;

public class UserRegistration {

    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private AccountType accountType;
    private Integer age;

    public UserRegistration() {
    }

    public UserRegistration(String username, String email, String password,
            String confirmPassword, AccountType accountType, Integer age) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.accountType = accountType;
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Integer getAge() {
        return age;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
