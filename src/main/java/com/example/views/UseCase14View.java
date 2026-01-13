package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "use-case-14", layout = MainLayout.class)
@PageTitle("Use Case 14: Multi-Step Wizard with Validation")
@Menu(order = 60, title = "UC 14: Multi-Step Wizard")
public class UseCase14View extends VerticalLayout {

    enum Step { PERSONAL_INFO, COMPANY_INFO, PLAN_SELECTION, REVIEW }
    enum Plan { STARTER, PROFESSIONAL, ENTERPRISE }

    record FormData(String firstName, String lastName, String email,
                   String companyName, String companySize, String industry,
                   Plan selectedPlan) {}

    public UseCase14View() {
        // Create signals for current step and form data
        WritableSignal<Step> currentStepSignal = Signal.create(Step.PERSONAL_INFO);
        WritableSignal<String> firstNameSignal = Signal.create("");
        WritableSignal<String> lastNameSignal = Signal.create("");
        WritableSignal<String> emailSignal = Signal.create("");
        WritableSignal<String> companyNameSignal = Signal.create("");
        WritableSignal<String> companySizeSignal = Signal.create("");
        WritableSignal<String> industrySignal = Signal.create("");
        WritableSignal<Plan> planSignal = Signal.create(Plan.STARTER);

        // Validation signals
        ReadableSignal<Boolean> step1ValidSignal = Signal.compute(() ->
            !firstNameSignal.get().isEmpty() &&
            !lastNameSignal.get().isEmpty() &&
            emailSignal.get().contains("@")
        );

        ReadableSignal<Boolean> step2ValidSignal = Signal.compute(() ->
            !companyNameSignal.get().isEmpty() &&
            !companySizeSignal.get().isEmpty() &&
            !industrySignal.get().isEmpty()
        );

        ReadableSignal<Boolean> step3ValidSignal = Signal.create(true); // Plan always selected

        // Step 1: Personal Info
        VerticalLayout step1Layout = new VerticalLayout();
        step1Layout.add(new H3("Step 1: Personal Information"));

        TextField firstNameField = new TextField("First Name");
        firstNameField.bindValue(firstNameSignal);

        TextField lastNameField = new TextField("Last Name");
        lastNameField.bindValue(lastNameSignal);

        EmailField emailField = new EmailField("Email");
        emailField.bindValue(emailSignal);

        step1Layout.add(firstNameField, lastNameField, emailField);
        step1Layout.bindVisible(currentStepSignal.map(step -> step == Step.PERSONAL_INFO));

        // Step 2: Company Info
        VerticalLayout step2Layout = new VerticalLayout();
        step2Layout.add(new H3("Step 2: Company Information"));

        TextField companyNameField = new TextField("Company Name");
        companyNameField.bindValue(companyNameSignal);

        ComboBox<String> companySizeSelect = new ComboBox<>("Company Size",
            List.of("1-10", "11-50", "51-200", "201-1000", "1000+"));
        companySizeSelect.bindValue(companySizeSignal);

        ComboBox<String> industrySelect = new ComboBox<>("Industry",
            List.of("Technology", "Healthcare", "Finance", "Retail", "Manufacturing", "Other"));
        industrySelect.bindValue(industrySignal);

        step2Layout.add(companyNameField, companySizeSelect, industrySelect);
        step2Layout.bindVisible(currentStepSignal.map(step -> step == Step.COMPANY_INFO));

        // Step 3: Plan Selection
        VerticalLayout step3Layout = new VerticalLayout();
        step3Layout.add(new H3("Step 3: Select Your Plan"));

        ComboBox<Plan> planSelect = new ComboBox<>("Plan", Plan.values());
        planSelect.setValue(Plan.STARTER);
        planSelect.bindValue(planSignal);

        Span planDescription = new Span();
        planDescription.bindText(planSignal.map(plan -> switch (plan) {
            case STARTER -> "Perfect for small teams - $29/month";
            case PROFESSIONAL -> "For growing businesses - $99/month";
            case ENTERPRISE -> "Custom solutions - Contact sales";
        }));

        step3Layout.add(planSelect, planDescription);
        step3Layout.bindVisible(currentStepSignal.map(step -> step == Step.PLAN_SELECTION));

        // Step 4: Review
        VerticalLayout step4Layout = new VerticalLayout();
        step4Layout.add(new H3("Step 4: Review Your Information"));

        Div reviewDiv = new Div();
        reviewDiv.bindText(Signal.compute(() ->
            "Name: " + firstNameSignal.get() + " " + lastNameSignal.get() + "\n" +
            "Email: " + emailSignal.get() + "\n" +
            "Company: " + companyNameSignal.get() + "\n" +
            "Size: " + companySizeSignal.get() + "\n" +
            "Industry: " + industrySignal.get() + "\n" +
            "Plan: " + planSignal.get()
        ));

        step4Layout.add(reviewDiv);
        step4Layout.bindVisible(currentStepSignal.map(step -> step == Step.REVIEW));

        // Navigation buttons
        HorizontalLayout navigationLayout = new HorizontalLayout();

        Button previousButton = new Button("Previous", e -> {
            Step current = currentStepSignal.get();
            switch (current) {
                case COMPANY_INFO -> currentStepSignal.set(Step.PERSONAL_INFO);
                case PLAN_SELECTION -> currentStepSignal.set(Step.COMPANY_INFO);
                case REVIEW -> currentStepSignal.set(Step.PLAN_SELECTION);
            }
        });
        previousButton.bindVisible(currentStepSignal.map(step -> step != Step.PERSONAL_INFO));

        Button nextButton = new Button("Next", e -> {
            Step current = currentStepSignal.get();
            switch (current) {
                case PERSONAL_INFO -> currentStepSignal.set(Step.COMPANY_INFO);
                case COMPANY_INFO -> currentStepSignal.set(Step.PLAN_SELECTION);
                case PLAN_SELECTION -> currentStepSignal.set(Step.REVIEW);
            }
        });
        nextButton.bindVisible(currentStepSignal.map(step -> step != Step.REVIEW));
        nextButton.bindEnabled(Signal.compute(() -> {
            Step current = currentStepSignal.get();
            return switch (current) {
                case PERSONAL_INFO -> step1ValidSignal.get();
                case COMPANY_INFO -> step2ValidSignal.get();
                case PLAN_SELECTION -> step3ValidSignal.get();
                case REVIEW -> false;
            };
        }));

        Button submitButton = new Button("Submit", e -> {
            // Handle form submission
            System.out.println("Form submitted!");
        });
        submitButton.bindVisible(currentStepSignal.map(step -> step == Step.REVIEW));

        navigationLayout.add(previousButton, nextButton, submitButton);

        // Progress indicator
        Span progressIndicator = new Span();
        progressIndicator.bindText(currentStepSignal.map(step -> {
            int stepNumber = switch (step) {
                case PERSONAL_INFO -> 1;
                case COMPANY_INFO -> 2;
                case PLAN_SELECTION -> 3;
                case REVIEW -> 4;
            };
            return "Step " + stepNumber + " of 4";
        }));
        progressIndicator.getStyle().set("font-weight", "bold");

        add(progressIndicator, step1Layout, step2Layout, step3Layout, step4Layout, navigationLayout);
    }
}
