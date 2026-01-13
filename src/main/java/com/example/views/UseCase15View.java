package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
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

@Route(value = "use-case-15", layout = MainLayout.class)
@PageTitle("Use Case 15: Form with Binder Integration and Signal Validation")
@Menu(order = 61, title = "UC 15: Binder Integration")
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
        WritableSignal<String> usernameSignal = Signal.create("");
        WritableSignal<String> emailSignal = Signal.create("");
        WritableSignal<String> passwordSignal = Signal.create("");
        WritableSignal<String> confirmPasswordSignal = Signal.create("");
        WritableSignal<AccountType> accountTypeSignal = Signal.create(AccountType.PERSONAL);
        WritableSignal<Integer> ageSignal = Signal.create(null);

        // Bind fields to signals
        usernameField.bindValue(usernameSignal);
        emailField.bindValue(emailSignal);
        passwordField.bindValue(passwordSignal);
        confirmPasswordField.bindValue(confirmPasswordSignal);
        accountTypeSelect.bindValue(accountTypeSignal);
        ageField.bindValue(ageSignal);

        // Validation signals
        ReadableSignal<Boolean> usernameValidSignal = Signal.compute(() -> {
            String username = usernameSignal.get();
            return username != null && username.length() >= 3;
        });

        ReadableSignal<Boolean> emailValidSignal = Signal.compute(() -> {
            String email = emailSignal.get();
            return email != null && email.contains("@") && email.contains(".");
        });

        ReadableSignal<Boolean> passwordValidSignal = Signal.compute(() -> {
            String password = passwordSignal.get();
            return password != null && password.length() >= 8;
        });

        ReadableSignal<Boolean> passwordsMatchSignal = Signal.compute(() -> {
            String password = passwordSignal.get();
            String confirmPassword = confirmPasswordSignal.get();
            return password != null && password.equals(confirmPassword);
        });

        ReadableSignal<Boolean> ageValidSignal = Signal.compute(() -> {
            Integer age = ageSignal.get();
            AccountType accountType = accountTypeSignal.get();
            if (age == null) return false;
            // Business accounts require age >= 18
            if (accountType == AccountType.BUSINESS) {
                return age >= 18;
            }
            // Personal accounts require age >= 13
            return age >= 13;
        });

        ReadableSignal<Boolean> formValidSignal = Signal.compute(() ->
            usernameValidSignal.get() &&
            emailValidSignal.get() &&
            passwordValidSignal.get() &&
            passwordsMatchSignal.get() &&
            ageValidSignal.get()
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
        confirmPasswordError.bindVisible(Signal.compute(() -> {
            String password = passwordSignal.get();
            String confirmPassword = confirmPasswordSignal.get();
            return confirmPassword != null && !confirmPassword.isEmpty() &&
                   password != null && !password.equals(confirmPassword);
        }));

        Span ageError = new Span();
        ageError.bindText(Signal.compute(() -> {
            AccountType accountType = accountTypeSignal.get();
            if (accountType == AccountType.BUSINESS) {
                return "Business accounts require age 18 or older";
            }
            return "Personal accounts require age 13 or older";
        }));
        ageError.getStyle().set("color", "red");
        ageError.bindVisible(Signal.compute(() -> {
            Integer age = ageSignal.get();
            AccountType accountType = accountTypeSignal.get();
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
        statusLabel.bindText(formValidSignal.map(valid ->
            valid ? "Form is valid - Ready to submit" : "Please complete all required fields correctly"
        ));
        statusLabel.bindAttribute("style", formValidSignal.map(valid ->
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
