package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;

@Route(value = "use-case-03", layout = MainLayout.class)
@PageTitle("Use Case 3: Dynamic Button State")
@Menu(order = 12, title = "UC 3: Dynamic Button State")
public class UseCase03View extends VerticalLayout {

    enum SubmissionState { IDLE, SUBMITTING, SUCCESS, ERROR }

    public UseCase03View() {
        // Field signals
        WritableSignal<String> emailSignal = Signal.create("");
        WritableSignal<String> passwordSignal = Signal.create("");
        WritableSignal<String> confirmPasswordSignal = Signal.create("");
        WritableSignal<SubmissionState> submissionStateSignal =
            Signal.create(SubmissionState.IDLE);

        // Computed validity signal
        Signal<Boolean> isValidSignal = Signal.compute(() -> {
            String email = emailSignal.getValue();
            String password = passwordSignal.getValue();
            String confirm = confirmPasswordSignal.getValue();

            return email.contains("@")
                && password.length() >= 8
                && password.equals(confirm);
        });

        // Form fields
        EmailField emailField = new EmailField("Email");
        emailField.bindValue(emailSignal);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.bindValue(passwordSignal);

        PasswordField confirmField = new PasswordField("Confirm Password");
        confirmField.bindValue(confirmPasswordSignal);

        // Submit button with multiple signal bindings
        Button submitButton = new Button();

        // Bind enabled state: enabled when valid AND not submitting
        submitButton.bindEnabled(Signal.compute(() ->
            isValidSignal.getValue()
            && submissionStateSignal.getValue() == SubmissionState.IDLE
        ));

        // Bind button text based on submission state
        submitButton.bindText(submissionStateSignal.map(state -> switch(state) {
            case IDLE -> "Create Account";
            case SUBMITTING -> "Creating...";
            case SUCCESS -> "Success!";
            case ERROR -> "Retry";
        }));

        // Bind theme variant
        submitButton.bindThemeName(submissionStateSignal.map(state ->
            state == SubmissionState.SUCCESS ? "success" : "primary"
        ));

        submitButton.addClickListener(e -> {
            submissionStateSignal.setValue(SubmissionState.SUBMITTING);
            // Simulate async submission
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    getUI().ifPresent(ui -> ui.access(() ->
                        submissionStateSignal.setValue(SubmissionState.SUCCESS)
                    ));
                } catch (Exception ex) {
                    getUI().ifPresent(ui -> ui.access(() ->
                        submissionStateSignal.setValue(SubmissionState.ERROR)
                    ));
                }
            });
        });

        add(emailField, passwordField, confirmField, submitButton);
    }
}
