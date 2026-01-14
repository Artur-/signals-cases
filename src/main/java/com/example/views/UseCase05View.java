package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;

import com.example.MissingAPI;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

@Route(value = "use-case-5", layout = MainLayout.class)
@PageTitle("Use Case 5: Cascading Location Selector")
@Menu(order = 5, title = "UC 5: Cascading Selector")
@PermitAll
public class UseCase05View extends VerticalLayout {

    public UseCase05View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 5: Cascading Location Selector");

        Paragraph description = new Paragraph(
                "This use case demonstrates cascading dropdowns where each selection filters the next dropdown. "
                        + "Select a country to see states, then select a state to see cities. "
                        + "All filtering is reactive through computed signals.");

        // Create signals for selections - use first country as default
        List<String> countries = loadCountries();
        WritableSignal<String> countrySignal = new ValueSignal<>(
                countries.get(0));
        WritableSignal<String> stateSignal = new ValueSignal<>("");
        WritableSignal<String> citySignal = new ValueSignal<>("");

        // Country selector
        ComboBox<String> countrySelect = new ComboBox<>("Country");
        countrySelect.setItems(countries);
        countrySelect.bindValue(countrySignal);

        // State selector - computed items based on country
        ComboBox<String> stateSelect = new ComboBox<>("State/Province");
        stateSelect.setItems(List.of()); // Initialize with empty items
        MissingAPI.bindItems(stateSelect, countrySignal.map(country -> {
            stateSignal.value(""); // Reset state when country changes
            return country != null && !country.isEmpty() ? loadStates(country)
                    : List.of();
        }));
        stateSelect.bindValue(stateSignal);
        stateSelect.bindEnabled(countrySignal
                .map(country -> country != null && !country.isEmpty()));

        // City selector - computed items based on state
        ComboBox<String> citySelect = new ComboBox<>("City");
        citySelect.setItems(List.of()); // Initialize with empty items
        MissingAPI.bindItems(citySelect, stateSignal.map(state -> {
            citySignal.value(""); // Reset city when state changes
            return state != null && !state.isEmpty()
                    ? loadCities(countrySignal.value(), state)
                    : List.of();
        }));
        citySelect.bindValue(citySignal);
        citySelect.bindEnabled(
                stateSignal.map(state -> state != null && !state.isEmpty()));

        add(title, description, countrySelect, stateSelect, citySelect);
    }

    private List<String> loadCountries() {
        // Stub implementation - returns mock data
        return List.of("United States", "Canada", "United Kingdom", "Germany");
    }

    private List<String> loadStates(String country) {
        // Stub implementation - returns mock data
        Map<String, List<String>> statesByCountry = Map.of("United States",
                List.of("California", "Texas", "New York", "Florida"), "Canada",
                List.of("Ontario", "Quebec", "British Columbia", "Alberta"),
                "United Kingdom",
                List.of("England", "Scotland", "Wales", "Northern Ireland"),
                "Germany", List.of("Bavaria", "Berlin", "Hamburg", "Hesse"));
        return statesByCountry.getOrDefault(country, List.of());
    }

    private List<String> loadCities(String country, String state) {
        // Stub implementation - returns mock data
        Map<String, Map<String, List<String>>> citiesByState = Map.of(
                "United States",
                Map.of("California",
                        List.of("Los Angeles", "San Francisco", "San Diego",
                                "Sacramento"),
                        "Texas",
                        List.of("Houston", "Austin", "Dallas", "San Antonio"),
                        "New York",
                        List.of("New York City", "Buffalo", "Rochester",
                                "Albany"),
                        "Florida",
                        List.of("Miami", "Orlando", "Tampa", "Jacksonville")),
                "Canada",
                Map.of("Ontario",
                        List.of("Toronto", "Ottawa", "Mississauga", "Hamilton"),
                        "Quebec",
                        List.of("Montreal", "Quebec City", "Laval", "Gatineau"),
                        "British Columbia",
                        List.of("Vancouver", "Victoria", "Kelowna", "Burnaby"),
                        "Alberta", List.of("Calgary", "Edmonton", "Red Deer",
                                "Lethbridge")));
        return citiesByState.getOrDefault(country, Map.of()).getOrDefault(state,
                List.of());
    }
}
