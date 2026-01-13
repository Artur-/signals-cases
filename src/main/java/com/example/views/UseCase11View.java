package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;

@Route(value = "use-case-11", layout = MainLayout.class)
@PageTitle("Use Case 11: Cascading Location Selector")
@Menu(order = 50, title = "UC 11: Cascading Selector")
public class UseCase11View extends VerticalLayout {

    public UseCase11View() {
        // Create signals for selections
        WritableSignal<String> countrySignal = Signal.create(null);
        WritableSignal<String> stateSignal = Signal.create(null);
        WritableSignal<String> citySignal = Signal.create(null);

        // Country selector
        ComboBox<String> countrySelect = new ComboBox<>("Country");
        countrySelect.setItems(loadCountries());
        countrySelect.bindValue(countrySignal);

        // State selector - computed items based on country
        ComboBox<String> stateSelect = new ComboBox<>("State/Province");
        stateSelect.bindItems(countrySignal.map(country -> {
            stateSignal.set(null); // Reset state when country changes
            return country != null ? loadStates(country) : List.of();
        }));
        stateSelect.bindValue(stateSignal);
        stateSelect.bindEnabled(countrySignal.map(country -> country != null));

        // City selector - computed items based on state
        ComboBox<String> citySelect = new ComboBox<>("City");
        citySelect.bindItems(stateSignal.map(state -> {
            citySignal.set(null); // Reset city when state changes
            return state != null ? loadCities(countrySignal.get(), state) : List.of();
        }));
        citySelect.bindValue(citySignal);
        citySelect.bindEnabled(stateSignal.map(state -> state != null));

        add(countrySelect, stateSelect, citySelect);
    }

    private List<String> loadCountries() {
        // Stub implementation - returns mock data
        return List.of("United States", "Canada", "United Kingdom", "Germany");
    }

    private List<String> loadStates(String country) {
        // Stub implementation - returns mock data
        Map<String, List<String>> statesByCountry = Map.of(
            "United States", List.of("California", "Texas", "New York", "Florida"),
            "Canada", List.of("Ontario", "Quebec", "British Columbia", "Alberta"),
            "United Kingdom", List.of("England", "Scotland", "Wales", "Northern Ireland"),
            "Germany", List.of("Bavaria", "Berlin", "Hamburg", "Hesse")
        );
        return statesByCountry.getOrDefault(country, List.of());
    }

    private List<String> loadCities(String country, String state) {
        // Stub implementation - returns mock data
        Map<String, Map<String, List<String>>> citiesByState = Map.of(
            "United States", Map.of(
                "California", List.of("Los Angeles", "San Francisco", "San Diego", "Sacramento"),
                "Texas", List.of("Houston", "Austin", "Dallas", "San Antonio"),
                "New York", List.of("New York City", "Buffalo", "Rochester", "Albany"),
                "Florida", List.of("Miami", "Orlando", "Tampa", "Jacksonville")
            ),
            "Canada", Map.of(
                "Ontario", List.of("Toronto", "Ottawa", "Mississauga", "Hamilton"),
                "Quebec", List.of("Montreal", "Quebec City", "Laval", "Gatineau"),
                "British Columbia", List.of("Vancouver", "Victoria", "Kelowna", "Burnaby"),
                "Alberta", List.of("Calgary", "Edmonton", "Red Deer", "Lethbridge")
            )
        );
        return citiesByState.getOrDefault(country, Map.of()).getOrDefault(state, List.of());
    }
}
