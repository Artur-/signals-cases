package com.example.usecase09;

import jakarta.annotation.security.PermitAll;

import com.example.MissingAPI;
import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

@Route(value = "use-case-09", layout = MainLayout.class)
@PageTitle("Use Case 9: Form with Binder Integration and Signal Validation")
@Menu(order = 9, title = "UC 9: Binder Integration")
@PermitAll
public class UseCase09View extends VerticalLayout {

    public UseCase09View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2(
                "Use Case 9: Form with Binder Integration and Signal Validation");

        Paragraph description = new Paragraph(
                "This use case demonstrates Vaadin Binder integration with reactive signal-based validation. "
                        + "The form validates in real-time as you type, showing specific error messages for each field. "
                        + "Note how the age requirement changes based on account type (Personal: 13+, Business: 18+). "
                        + "The submit button is enabled only when all fields pass validation.");

        // Create binder
        Binder<UserRegistration> binder = new Binder<>(UserRegistration.class);

        // Create form fields
        TextField usernameField = new TextField("Username");
        EmailField emailField = new EmailField("Email");
        PasswordField passwordField = new PasswordField("Password");
        PasswordField confirmPasswordField = new PasswordField(
                "Confirm Password");
        ComboBox<AccountType> accountTypeSelect = new ComboBox<>("Account Type",
                AccountType.values());
        accountTypeSelect.setValue(AccountType.PERSONAL);
        IntegerField ageField = new IntegerField("Age");
        ageField.setValue(0);

        Signal<String> usernameSignal = MissingAPI.binderBind(binder,
                usernameField, "username");
        Signal<String> emailSignal = MissingAPI.binderBind(binder, emailField,
                "email");
        Signal<String> passwordSignal = MissingAPI.binderBind(binder,
                passwordField, "password");
        Signal<String> confirmPasswordSignal = MissingAPI.binderBind(binder,
                confirmPasswordField, "confirmPassword");
        Signal<AccountType> accountTypeSignal = MissingAPI.binderBind(binder,
                accountTypeSelect, "accountType");
        Signal<Integer> ageSignal = MissingAPI.binderBind(binder, ageField,
                "age");

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
            if (age == null)
                return false;
            // Business accounts require age >= 18
            if (accountType == AccountType.BUSINESS) {
                return age >= 18;
            }
            // Personal accounts require age >= 13
            return age >= 13;
        });

        WritableSignal<Boolean> formValidSignal = new ValueSignal<>(false);

        // cross-field validation effects
        MissingAPI.binderValidateInEffect(binder, confirmPasswordField,
                "confirmPassword", passwordsMatchSignal);
        MissingAPI.binderValidateInEffect(binder, ageField, "age",
                accountTypeSignal);

        // binder bindings
        binder.forField(usernameField)
                .withValidator(value -> usernameValidSignal.value(),
                        "Username must be at least 3 characters")
                .bind("username");
        binder.forField(emailField)
                .withValidator(value -> emailValidSignal.value(),
                        "Please enter a valid email address")
                .bind("email");
        binder.forField(passwordField)
                .withValidator(value -> passwordValidSignal.value(),
                        "Password must be at least 8 characters")
                .bind("password");
        binder.forField(confirmPasswordField)
                .withValidator(value -> passwordsMatchSignal.value(),
                        "Passwords do not match")
                .bind("confirmPassword");
        binder.forField(ageField)
                .withValidator(value -> ageValidSignal.value(), value -> {
                    AccountType accountType = accountTypeSignal.value();
                    if (accountType == AccountType.BUSINESS) {
                        return "Business accounts require age 18 or older";
                    }
                    return "Personal accounts require age 13 or older";
                }).bind("age");

        binder.addStatusChangeListener(event -> {
            formValidSignal.value(event.getBinder().isValid());
        });

        binder.readBean(
                new UserRegistration("", "", "", "", AccountType.PERSONAL, 0));

        // Submit button
        Button submitButton = new Button("Register", e -> {
            // Handle registration
            if (binder.isValid()) {
                UserRegistration userRegistration = new UserRegistration();
                binder.writeBeanIfValid(userRegistration);
                Notification.show("Registration submitted!");

                binder.readBean(new UserRegistration("", "", "", "",
                        AccountType.PERSONAL, 0));
            } else {
                Notification.show(
                        "Please fix validation errors before submitting.");
            }
        });
        submitButton.bindEnabled(formValidSignal);

        // Form status
        Div statusDiv = new Div();
        Span statusLabel = new Span();
        statusLabel.bindText(formValidSignal
                .map(valid -> valid ? "Form is valid - Ready to submit"
                        : "Please complete all required fields correctly"));
        statusLabel.getStyle().bind("color",
                formValidSignal.map(valid -> valid ? "green" : "orange"));
        statusLabel.getStyle().bind("font-weight",
                formValidSignal.map(valid -> valid ? "bold" : "normal"));
        statusDiv.add(statusLabel);

        add(title, description, usernameField, emailField, passwordField,
                confirmPasswordField, accountTypeSelect, ageField, statusDiv,
                submitButton);
    }
}
