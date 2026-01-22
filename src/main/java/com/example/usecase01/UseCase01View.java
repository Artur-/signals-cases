package com.example.usecase01;

import jakarta.annotation.security.PermitAll;

import java.util.concurrent.CompletableFuture;

import com.example.MissingAPI;
import com.example.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

@Route(value = "use-case-01", layout = MainLayout.class)
@PageTitle("Use Case 1: Dynamic Button State")
@Menu(order = 1, title = "UC 1: Dynamic Button State")
@PermitAll
public class UseCase01View extends VerticalLayout {

    public UseCase01View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 1: Dynamic Button State");

        Paragraph description = new Paragraph(
                "This use case demonstrates reactive form validation with dynamic button states. "
                        + "The submit button is enabled only when all fields are valid: email contains '@', "
                        + "password is at least 8 characters, and passwords match. "
                        + "The button also reactively changes text during form submission.");

        // Field signals
        WritableSignal<String> emailSignal = new ValueSignal<>("");
        WritableSignal<String> passwordSignal = new ValueSignal<>("");
        WritableSignal<String> confirmPasswordSignal = new ValueSignal<>("");
        WritableSignal<SubmissionState> submissionStateSignal = new ValueSignal<>(
                SubmissionState.IDLE);

        // Individual field validity signals
        Signal<Boolean> isPasswordValidSignal = Signal.computed(() -> {
            String password = passwordSignal.value();
            return password.isEmpty() || password.length() >= 8;
        });

        Signal<Boolean> isConfirmValidSignal = Signal.computed(() -> {
            String password = passwordSignal.value();
            String confirm = confirmPasswordSignal.value();
            return confirm.isEmpty() || password.equals(confirm);
        });

        // Computed validity signal
        Signal<Boolean> isValidSignal = Signal.computed(() -> {
            String email = emailSignal.value();
            String password = passwordSignal.value();
            String confirm = confirmPasswordSignal.value();

            return email.contains("@") && password.length() >= 8
                    && password.equals(confirm);
        });

        // Form fields
        EmailField emailField = new EmailField("Email");
        emailField.setHelperText("Valid email address required");
        emailField.bindValue(emailSignal);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setHelperText("Minimum 8 characters required");
        passwordField.bindValue(passwordSignal);
        passwordField.setMinLength(8);

        PasswordField confirmField = new PasswordField("Confirm Password");
        confirmField.setHelperText("Must match password");
        confirmField.bindValue(confirmPasswordSignal);
        confirmField.setErrorMessage("Passwords do not match");
        MissingAPI.bindInvalid(confirmField,
                isConfirmValidSignal.map(valid -> !valid));

        // Submit button with multiple signal bindings
        Button submitButton = new Button();

        // Bind enabled state: enabled when valid AND not submitting
        submitButton.bindEnabled(Signal.computed(() -> isValidSignal.value()
                && (submissionStateSignal.value() != SubmissionState.SUBMITTING)));

        // Bind button text based on submission state
        submitButton
                .bindText(submissionStateSignal.map(state -> switch (state) {
                case IDLE -> "Create Account";
                case SUBMITTING -> "Creating...";
                case SUCCESS -> "Success!";
                case ERROR -> "Retry";
                }));

        // Bind theme variant
        submitButton.bindThemeName("success", submissionStateSignal
                .map(SubmissionState.SUCCESS::equals));
        submitButton.bindThemeName("primary", submissionStateSignal
                .map(state -> state != SubmissionState.SUCCESS));

        submitButton.addClickListener(e -> {
            submissionStateSignal.value(SubmissionState.SUBMITTING);
            // Simulate async submission
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    submissionStateSignal.value(SubmissionState.SUCCESS);

                    // Show success notification and reset form
                    getUI().ifPresent(ui -> ui.access(() -> {
                        Notification notification = Notification.show(
                                "Account created successfully!");
                        notification.addThemeVariants(
                                NotificationVariant.LUMO_SUCCESS);
                        notification.setDuration(3000);

                        // Reset form fields
                        emailSignal.value("");
                        passwordSignal.value("");
                        confirmPasswordSignal.value("");

                        // Reset submission state back to IDLE
                        submissionStateSignal.value(SubmissionState.IDLE);
                    }));
                } catch (Exception ex) {
                    submissionStateSignal.value(SubmissionState.ERROR);
                }
            });
        });

        add(title, description, emailField, passwordField, confirmField,
                submitButton);
    }
}
