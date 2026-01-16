package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.util.List;

import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ReferenceSignal;
import com.vaadin.signals.WritableSignal;

@Route(value = "use-case-02", layout = MainLayout.class)
@PageTitle("Use Case 2: Progressive Disclosure with Nested Conditions")
@Menu(order = 2, title = "UC 2: Nested Conditions")
@PermitAll
public class UseCase02View extends VerticalLayout {

    enum VisaType {
        H1B, L1, O1, OTHER
    }

    // Bean to hold all form data
    public static class VisaApplicationData {
        private Boolean needsVisa = false;
        private String currentVisaStatus = "";
        private VisaType visaType = VisaType.H1B;

        // H1B fields
        private Boolean hasH1BPreviously = false;
        private String h1bSpecialtyOccupation = "";
        private String previousEmployer = "";
        private String previousPetitionNumber = "";
        private String previousH1BStartDate = "";

        // L1 fields
        private String parentCompanyName = "";
        private String yearsWithParentCompany = "";
        private String l1Category = "";

        // O1 fields
        private String fieldOfExtraordinaryAbility = "";
        private String majorAwards = "";
        private String publications = "";

        // Getters and setters
        public Boolean getNeedsVisa() {
            return needsVisa;
        }

        public void setNeedsVisa(Boolean needsVisa) {
            this.needsVisa = needsVisa;
        }

        public String getCurrentVisaStatus() {
            return currentVisaStatus;
        }

        public void setCurrentVisaStatus(String currentVisaStatus) {
            this.currentVisaStatus = currentVisaStatus;
        }

        public VisaType getVisaType() {
            return visaType;
        }

        public void setVisaType(VisaType visaType) {
            this.visaType = visaType;
        }

        public Boolean getHasH1BPreviously() {
            return hasH1BPreviously;
        }

        public void setHasH1BPreviously(Boolean hasH1BPreviously) {
            this.hasH1BPreviously = hasH1BPreviously;
        }

        public String getH1bSpecialtyOccupation() {
            return h1bSpecialtyOccupation;
        }

        public void setH1bSpecialtyOccupation(String h1bSpecialtyOccupation) {
            this.h1bSpecialtyOccupation = h1bSpecialtyOccupation;
        }

        public String getPreviousEmployer() {
            return previousEmployer;
        }

        public void setPreviousEmployer(String previousEmployer) {
            this.previousEmployer = previousEmployer;
        }

        public String getPreviousPetitionNumber() {
            return previousPetitionNumber;
        }

        public void setPreviousPetitionNumber(String previousPetitionNumber) {
            this.previousPetitionNumber = previousPetitionNumber;
        }

        public String getPreviousH1BStartDate() {
            return previousH1BStartDate;
        }

        public void setPreviousH1BStartDate(String previousH1BStartDate) {
            this.previousH1BStartDate = previousH1BStartDate;
        }

        public String getParentCompanyName() {
            return parentCompanyName;
        }

        public void setParentCompanyName(String parentCompanyName) {
            this.parentCompanyName = parentCompanyName;
        }

        public String getYearsWithParentCompany() {
            return yearsWithParentCompany;
        }

        public void setYearsWithParentCompany(String yearsWithParentCompany) {
            this.yearsWithParentCompany = yearsWithParentCompany;
        }

        public String getL1Category() {
            return l1Category;
        }

        public void setL1Category(String l1Category) {
            this.l1Category = l1Category;
        }

        public String getFieldOfExtraordinaryAbility() {
            return fieldOfExtraordinaryAbility;
        }

        public void setFieldOfExtraordinaryAbility(
                String fieldOfExtraordinaryAbility) {
            this.fieldOfExtraordinaryAbility = fieldOfExtraordinaryAbility;
        }

        public String getMajorAwards() {
            return majorAwards;
        }

        public void setMajorAwards(String majorAwards) {
            this.majorAwards = majorAwards;
        }

        public String getPublications() {
            return publications;
        }

        public void setPublications(String publications) {
            this.publications = publications;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Visa Application Data:\n");
            sb.append("--------------------\n");
            sb.append("Needs Visa: ").append(needsVisa).append("\n");

            if (needsVisa) {
                sb.append("Current Visa Status: ").append(currentVisaStatus)
                        .append("\n");
                sb.append("Visa Type: ").append(visaType).append("\n\n");

                if (visaType == VisaType.H1B) {
                    sb.append("H1-B Details:\n");
                    sb.append("  Specialty Occupation: ")
                            .append(h1bSpecialtyOccupation).append("\n");
                    sb.append("  Had H1-B Previously: ")
                            .append(hasH1BPreviously).append("\n");
                    if (hasH1BPreviously) {
                        sb.append("  Previous Employer: ")
                                .append(previousEmployer).append("\n");
                        sb.append("  Previous Petition #: ")
                                .append(previousPetitionNumber).append("\n");
                        sb.append("  Previous Start Date: ")
                                .append(previousH1BStartDate).append("\n");
                    }
                } else if (visaType == VisaType.L1) {
                    sb.append("L1 Details:\n");
                    sb.append("  Parent Company: ").append(parentCompanyName)
                            .append("\n");
                    sb.append("  Years with Company: ")
                            .append(yearsWithParentCompany).append("\n");
                    sb.append("  L1 Category: ").append(l1Category)
                            .append("\n");
                } else if (visaType == VisaType.O1) {
                    sb.append("O1 Details:\n");
                    sb.append("  Field of Extraordinary Ability: ")
                            .append(fieldOfExtraordinaryAbility).append("\n");
                    sb.append("  Major Awards: ").append(majorAwards)
                            .append("\n");
                    sb.append("  Publications: ").append(publications)
                            .append("\n");
                }
            }

            return sb.toString();
        }
    }

    public static class Results {
        WritableSignal<String> text = new ReferenceSignal<>("");
        WritableSignal<Boolean> visible = new ReferenceSignal<>(false);
    }

    public UseCase02View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2(
                "Use Case 2: Progressive Disclosure with Nested Conditions");

        Paragraph description = new Paragraph(
                "This use case demonstrates progressive disclosure with multi-level conditional forms. "
                        + "Fields appear and disappear reactively based on previous selections. "
                        + "Check the visa sponsorship checkbox to reveal nested visa type selection, "
                        + "which then reveals type-specific fields in a cascading manner.");

        // Create binder and bean
        Binder<VisaApplicationData> binder = new Binder<>(
                VisaApplicationData.class);
        VisaApplicationData data = new VisaApplicationData();
        binder.setBean(data);

        // Signals for conditional visibility
        WritableSignal<Boolean> needsVisaSignal = new ReferenceSignal<>(false);
        WritableSignal<VisaType> visaTypeSignal = new ReferenceSignal<>(
                VisaType.H1B);
        WritableSignal<Boolean> hasH1BPreviouslySignal = new ReferenceSignal<>(
                false);

        // Base question: needs visa sponsorship
        Checkbox needsVisaCheckbox = new Checkbox(
                "Do you require visa sponsorship?");
        binder.forField(needsVisaCheckbox).bind(
                VisaApplicationData::getNeedsVisa,
                VisaApplicationData::setNeedsVisa);
        needsVisaCheckbox.bindValue(needsVisaSignal);

        // Level 1: Visa-related fields (shown when needsVisa is true)
        VerticalLayout visaSection = new VerticalLayout();

        ComboBox<VisaType> visaTypeSelect = new ComboBox<>("Visa Type",
                VisaType.values());
        visaTypeSelect.setValue(VisaType.H1B);
        binder.forField(visaTypeSelect).bind(VisaApplicationData::getVisaType,
                VisaApplicationData::setVisaType);
        visaTypeSelect.bindValue(visaTypeSignal);

        TextField currentVisaStatus = new TextField("Current Visa Status");
        binder.forField(currentVisaStatus).bind(
                VisaApplicationData::getCurrentVisaStatus,
                VisaApplicationData::setCurrentVisaStatus);

        visaSection.add(visaTypeSelect, currentVisaStatus);
        visaSection.bindVisible(needsVisaSignal);

        // Level 2: H1-B specific fields (shown when visa type is H1B)
        VerticalLayout h1bSection = new VerticalLayout();

        Checkbox hasH1BPreviouslyCheckbox = new Checkbox(
                "Have you held an H1-B visa before?");
        binder.forField(hasH1BPreviouslyCheckbox).bind(
                VisaApplicationData::getHasH1BPreviously,
                VisaApplicationData::setHasH1BPreviously);
        hasH1BPreviouslyCheckbox.bindValue(hasH1BPreviouslySignal);

        TextField h1bSpecialtyOccupation = new TextField(
                "Specialty Occupation");
        binder.forField(h1bSpecialtyOccupation).bind(
                VisaApplicationData::getH1bSpecialtyOccupation,
                VisaApplicationData::setH1bSpecialtyOccupation);

        h1bSection.add(hasH1BPreviouslyCheckbox, h1bSpecialtyOccupation);
        h1bSection.bindVisible(() -> needsVisaSignal.value()
                && visaTypeSignal.value() == VisaType.H1B);

        // Level 3: Previous H1-B details (shown when has H1-B previously)
        VerticalLayout previousH1BSection = new VerticalLayout();

        TextField previousEmployer = new TextField("Previous Employer");
        binder.forField(previousEmployer).bind(
                VisaApplicationData::getPreviousEmployer,
                VisaApplicationData::setPreviousEmployer);

        TextField previousPetitionNumber = new TextField(
                "Previous Petition Number");
        binder.forField(previousPetitionNumber).bind(
                VisaApplicationData::getPreviousPetitionNumber,
                VisaApplicationData::setPreviousPetitionNumber);

        TextField previousH1BStartDate = new TextField(
                "Previous H1-B Start Date");
        binder.forField(previousH1BStartDate).bind(
                VisaApplicationData::getPreviousH1BStartDate,
                VisaApplicationData::setPreviousH1BStartDate);

        previousH1BSection.add(previousEmployer, previousPetitionNumber,
                previousH1BStartDate);
        previousH1BSection.bindVisible(() -> needsVisaSignal.value()
                && visaTypeSignal.value() == VisaType.H1B
                && hasH1BPreviouslySignal.value());

        // Level 2: L1 specific fields (shown when visa type is L1)
        VerticalLayout l1Section = new VerticalLayout();

        TextField parentCompanyName = new TextField("Parent Company Name");
        binder.forField(parentCompanyName).bind(
                VisaApplicationData::getParentCompanyName,
                VisaApplicationData::setParentCompanyName);

        TextField yearsWithParentCompany = new TextField(
                "Years with Parent Company");
        binder.forField(yearsWithParentCompany).bind(
                VisaApplicationData::getYearsWithParentCompany,
                VisaApplicationData::setYearsWithParentCompany);

        ComboBox<String> l1Category = new ComboBox<>("L1 Category",
                List.of("L1-A (Manager)", "L1-B (Specialized Knowledge)"));
        binder.forField(l1Category).bind(VisaApplicationData::getL1Category,
                VisaApplicationData::setL1Category);

        l1Section.add(parentCompanyName, yearsWithParentCompany, l1Category);
        l1Section.bindVisible(() -> needsVisaSignal.value()
                && visaTypeSignal.value() == VisaType.L1);

        // Level 2: O1 specific fields (shown when visa type is O1)
        VerticalLayout o1Section = new VerticalLayout();

        TextField fieldOfExtraordinaryAbility = new TextField(
                "Field of Extraordinary Ability");
        binder.forField(fieldOfExtraordinaryAbility).bind(
                VisaApplicationData::getFieldOfExtraordinaryAbility,
                VisaApplicationData::setFieldOfExtraordinaryAbility);

        TextField majorAwards = new TextField("Major Awards/Recognition");
        binder.forField(majorAwards).bind(VisaApplicationData::getMajorAwards,
                VisaApplicationData::setMajorAwards);

        TextField publications = new TextField(
                "Publications or Media Coverage");
        binder.forField(publications).bind(VisaApplicationData::getPublications,
                VisaApplicationData::setPublications);

        o1Section.add(fieldOfExtraordinaryAbility, majorAwards, publications);
        o1Section.bindVisible(() -> needsVisaSignal.value()
                && visaTypeSignal.value() == VisaType.O1);

        // Display area for collected values
        Div resultDisplay = new Div();
        resultDisplay.getStyle().set("margin-top", "2em").set("padding", "1em")
                .set("border", "1px solid #ccc").set("border-radius", "4px")
                .set("background-color", "#f5f5f5");
        resultDisplay.setVisible(false);

        Pre resultText = new Pre();
        resultText.getStyle().set("margin", "0");
        resultDisplay.add(new H3("Collected Form Data"), resultText);
        Results results = new Results();
        resultText.bindText(results.text);
        ComponentEffect.bind(resultText, results.text,
                (component, signal) -> Notification.show(
                        "Form data displayed below", 2000,
                        Notification.Position.BOTTOM_START));
        resultDisplay.bindVisible(results.visible);

        // Show Values button
        Button showValuesButton = new Button("Show Collected Values", event -> {
            results.text.value(data.toString());
            results.visible.value(true);
        });
        showValuesButton.addThemeName("primary");

        add(title, description, needsVisaCheckbox, visaSection, h1bSection,
                previousH1BSection, l1Section, o1Section, showValuesButton,
                resultDisplay, new SourceCodeLink(getClass()));
    }
}
