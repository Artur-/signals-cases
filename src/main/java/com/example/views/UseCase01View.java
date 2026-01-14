package com.example.views;

import com.example.MissingAPI;


// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.util.concurrent.CompletableFuture;

@Route(value = "use-case-03", layout = MainLayout.class)
@PageTitle("Use Case 1: Dynamic Button State")
@Menu(order = 1, title = "UC 1: Dynamic Button State")
@PermitAll
public class UseCase01View extends VerticalLayout {

    enum SubmissionState { IDLE, SUBMITTING, SUCCESS, ERROR }

    public UseCase01View() {
        // Field signals
        WritableSignal<String> emailSignal = new ValueSignal<>("");
        WritableSignal<String> passwordSignal = new ValueSignal<>("");
        WritableSignal<String> confirmPasswordSignal = new ValueSignal<>("");
        WritableSignal<SubmissionState> submissionStateSignal =
            new ValueSignal<>(SubmissionState.IDLE);

        // Computed validity signal
        Signal<Boolean> isValidSignal = Signal.computed(() -> {
            String email = emailSignal.value();
            String password = passwordSignal.value();
            String confirm = confirmPasswordSignal.value();

            return email.contains("@")
                && password.length() >= 8
                && password.equals(confirm);
        });

        // Form fields
        EmailField emailField = new EmailField("Email");
        emailField.setHelperText("Valid email address required");
        MissingAPI.bindValue(emailField, emailSignal);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setHelperText("Minimum 8 characters required");
        MissingAPI.bindValue(passwordField, passwordSignal);

        PasswordField confirmField = new PasswordField("Confirm Password");
        confirmField.setHelperText("Must match password");
        MissingAPI.bindValue(confirmField, confirmPasswordSignal);

        // Submit button with multiple signal bindings
        Button submitButton = new Button();

        // Bind enabled state: enabled when valid AND not submitting
        submitButton.bindEnabled(Signal.computed(() ->
            isValidSignal.value()
            && submissionStateSignal.value() == SubmissionState.IDLE
        ));

        // Bind button text based on submission state
        MissingAPI.bindText(submitButton, submissionStateSignal.map(state -> switch(state) {
            case IDLE -> "Create Account";
            case SUBMITTING -> "Creating...";
            case SUCCESS -> "Success!";
            case ERROR -> "Retry";
        }));

        // Bind theme variant
        MissingAPI.bindThemeName(submitButton, submissionStateSignal.map(state ->
            state == SubmissionState.SUCCESS ? "success" : "primary"
        ));

        submitButton.addClickListener(e -> {
            submissionStateSignal.value(SubmissionState.SUBMITTING);
            // Simulate async submission
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    getUI().ifPresent(ui -> ui.access(() ->
                        submissionStateSignal.value(SubmissionState.SUCCESS)
                    ));
                } catch (Exception ex) {
                    getUI().ifPresent(ui -> ui.access(() ->
                        submissionStateSignal.value(SubmissionState.ERROR)
                    ));
                }
            });
        });

        add(emailField, passwordField, confirmField, submitButton);
    }
}
