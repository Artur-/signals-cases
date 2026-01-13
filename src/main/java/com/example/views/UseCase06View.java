package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "use-case-06", layout = MainLayout.class)
@PageTitle("Use Case 6: Dynamic Form with Conditional Subforms")
@Menu(order = 30, title = "UC 6: Conditional Subforms")
public class UseCase06View extends VerticalLayout {

    enum InsuranceType { AUTO, HOME, LIFE }

    public UseCase06View() {
        // Create signal for insurance type
        WritableSignal<InsuranceType> insuranceTypeSignal = Signal.create(InsuranceType.AUTO);

        // Insurance type selector
        ComboBox<InsuranceType> typeSelect = new ComboBox<>("Insurance Type", InsuranceType.values());
        typeSelect.setValue(InsuranceType.AUTO);
        typeSelect.bindValue(insuranceTypeSignal);

        // Auto insurance form
        VerticalLayout autoForm = new VerticalLayout();
        autoForm.add(
            new TextField("Vehicle Make"),
            new TextField("Vehicle Model"),
            new IntegerField("Vehicle Year"),
            new TextField("VIN"),
            new IntegerField("Annual Mileage")
        );
        autoForm.bindVisible(insuranceTypeSignal.map(type -> type == InsuranceType.AUTO));

        // Home insurance form
        VerticalLayout homeForm = new VerticalLayout();
        homeForm.add(
            new TextField("Property Address"),
            new IntegerField("Year Built"),
            new IntegerField("Square Footage"),
            new ComboBox<>("Construction Type", List.of("Wood Frame", "Brick", "Concrete")),
            new TextField("Proximity to Fire Station")
        );
        homeForm.bindVisible(insuranceTypeSignal.map(type -> type == InsuranceType.HOME));

        // Life insurance form
        VerticalLayout lifeForm = new VerticalLayout();
        lifeForm.add(
            new IntegerField("Age"),
            new ComboBox<>("Health Status", List.of("Excellent", "Good", "Fair", "Poor")),
            new TextField("Occupation"),
            new ComboBox<>("Smoker Status", List.of("Non-smoker", "Former smoker", "Current smoker")),
            new TextField("Beneficiary Name")
        );
        lifeForm.bindVisible(insuranceTypeSignal.map(type -> type == InsuranceType.LIFE));

        add(typeSelect, autoForm, homeForm, lifeForm);
    }
}
