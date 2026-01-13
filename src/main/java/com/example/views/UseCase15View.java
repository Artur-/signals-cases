package com.example.views;

import com.example.MissingAPI;


// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import jakarta.annotation.security.PermitAll;

@Route(value = "use-case-15", layout = MainLayout.class)
@PageTitle("Use Case 15: Form with Binder Integration and Signal Validation")
@Menu(order = 61, title = "UC 15: Binder Integration")
@PermitAll
public class UseCase15View extends VerticalLayout {

    enum AccountType { PERSONAL, BUSINESS }

    record UserRegistration(String username, String email, String password, String confirmPassword,
                           AccountType accountType, Integer age) {}

    public UseCase15View() {
        // Create binder
        Binder<UserRegistration> binder = new Binder<>(UserRegistration.class);

        // Create form fields
        TextField usernameField = new TextField("Username");
        EmailField emailField = new EmailField("Email");
        PasswordField passwordField = new PasswordField("Password");
        PasswordField confirmPasswordField = new PasswordField("Confirm Password");
        ComboBox<AccountType> accountTypeSelect = new ComboBox<>("Account Type", AccountType.values());
        accountTypeSelect.setValue(AccountType.PERSONAL);
        IntegerField ageField = new IntegerField("Age");

        // Create signals for reactive validation
        WritableSignal<String> usernameSignal = new ValueSignal<>("");
        WritableSignal<String> emailSignal = new ValueSignal<>("");
        WritableSignal<String> passwordSignal = new ValueSignal<>("");
        WritableSignal<String> confirmPasswordSignal = new ValueSignal<>("");
        WritableSignal<AccountType> accountTypeSignal = new ValueSignal<>(AccountType.PERSONAL);
        WritableSignal<Integer> ageSignal = new ValueSignal<>(null);

        // Bind fields to signals
        MissingAPI.bindValue(usernameField, usernameSignal);
        MissingAPI.bindValue(emailField, emailSignal);
        MissingAPI.bindValue(passwordField, passwordSignal);
        MissingAPI.bindValue(confirmPasswordField, confirmPasswordSignal);
        MissingAPI.bindValue(accountTypeSelect, accountTypeSignal);
        MissingAPI.bindValue(ageField, ageSignal);

        // Validation signals
        Signal<Boolean> usernameValidSignal = Signal.computed(() -> {
            String username = usernameSignal.value();
            return username != null && username.length() >= 3;
        });

        Signal<Boolean> emailValidSignal = Signal.computed(() -> {
            String email = emailSignal.value();
            return email != null && email.contains("@") && email.contains(".");
        });

        Signal<Boolean> passwordValidSignal = Signal.computed(() -> {
            String password = passwordSignal.value();
            return password != null && password.length() >= 8;
        });

        Signal<Boolean> passwordsMatchSignal = Signal.computed(() -> {
            String password = passwordSignal.value();
            String confirmPassword = confirmPasswordSignal.value();
            return password != null && password.equals(confirmPassword);
        });

        Signal<Boolean> ageValidSignal = Signal.computed(() -> {
            Integer age = ageSignal.value();
            AccountType accountType = accountTypeSignal.value();
            if (age == null) return false;
            // Business accounts require age >= 18
            if (accountType == AccountType.BUSINESS) {
                return age >= 18;
            }
            // Personal accounts require age >= 13
            return age >= 13;
        });

        Signal<Boolean> formValidSignal = Signal.computed(() ->
            usernameValidSignal.value() &&
            emailValidSignal.value() &&
            passwordValidSignal.value() &&
            passwordsMatchSignal.value() &&
            ageValidSignal.value()
        );

        // Validation feedback
        Span usernameError = new Span("Username must be at least 3 characters");
        usernameError.getStyle().set("color", "red");
        usernameError.bindVisible(usernameSignal.map(u -> u != null && !u.isEmpty() && u.length() < 3));

        Span emailError = new Span("Please enter a valid email address");
        emailError.getStyle().set("color", "red");
        emailError.bindVisible(emailSignal.map(e -> e != null && !e.isEmpty() && (!e.contains("@") || !e.contains("."))));

        Span passwordError = new Span("Password must be at least 8 characters");
        passwordError.getStyle().set("color", "red");
        passwordError.bindVisible(passwordSignal.map(p -> p != null && !p.isEmpty() && p.length() < 8));

        Span confirmPasswordError = new Span("Passwords do not match");
        confirmPasswordError.getStyle().set("color", "red");
        confirmPasswordError.bindVisible(Signal.computed(() -> {
            String password = passwordSignal.value();
            String confirmPassword = confirmPasswordSignal.value();
            return confirmPassword != null && !confirmPassword.isEmpty() &&
                   password != null && !password.equals(confirmPassword);
        }));

        Span ageError = new Span();
        MissingAPI.bindText(ageError, Signal.computed(() -> {
            AccountType accountType = accountTypeSignal.value();
            if (accountType == AccountType.BUSINESS) {
                return "Business accounts require age 18 or older";
            }
            return "Personal accounts require age 13 or older";
        }));
        ageError.getStyle().set("color", "red");
        ageError.bindVisible(Signal.computed(() -> {
            Integer age = ageSignal.value();
            AccountType accountType = accountTypeSignal.value();
            if (age == null || age == 0) return false;
            if (accountType == AccountType.BUSINESS) {
                return age < 18;
            }
            return age < 13;
        }));

        // Submit button
        Button submitButton = new Button("Register", e -> {
            // Handle registration
            System.out.println("Registration submitted!");
        });
        submitButton.bindEnabled(formValidSignal);

        // Form status
        Div statusDiv = new Div();
        Span statusLabel = new Span();
        MissingAPI.bindText(statusLabel, formValidSignal.map(valid ->
            valid ? "Form is valid - Ready to submit" : "Please complete all required fields correctly"
        ));
        MissingAPI.bindAttribute(statusLabel, "style", formValidSignal.map(valid ->
            valid ? "color: green; font-weight: bold;" : "color: orange;"
        ));
        statusDiv.add(statusLabel);

        add(
            new H3("User Registration"),
            usernameField, usernameError,
            emailField, emailError,
            passwordField, passwordError,
            confirmPasswordField, confirmPasswordError,
            accountTypeSelect,
            ageField, ageError,
            statusDiv,
            submitButton
        );
    }
}
