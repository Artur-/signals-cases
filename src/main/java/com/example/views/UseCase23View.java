package com.example.views;

import com.example.MissingAPI;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;
import jakarta.annotation.security.PermitAll;

/**
 * Use Case 23: Dynamic View Title
 *
 * Demonstrates reactive page title that:
 * - Updates based on view state
 * - Syncs with browser tab title
 * - Can be used by parent layout for header display
 *
 * Key Patterns:
 * - View exposes a title signal
 * - Browser document.title binding
 * - Signal composition (app name + view title)
 */
@Route(value = "use-case-23", layout = MainLayout.class)
@PageTitle("Use Case 23: Dynamic View Title")
@Menu(order = 35, title = "UC 23: Dynamic View Title")
@PermitAll
public class UseCase23View extends VerticalLayout {

    private final WritableSignal<String> viewTitleSignal = new ValueSignal<>("Document Viewer");
    private static final String APP_NAME = "Signal API Demo";

    public UseCase23View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 23: Dynamic View Title");

        Paragraph description = new Paragraph(
            "This use case demonstrates a dynamic page title that updates based on view state. " +
            "The title is shown in the browser tab as 'App Name - View Title' and updates reactively " +
            "as you select different document types."
        );

        // Document type selector
        Select<String> documentTypeSelect = new Select<>();
        documentTypeSelect.setLabel("Select Document Type");
        documentTypeSelect.setItems("Document Viewer", "Spreadsheet Editor", "Presentation Creator",
                                    "Code Editor", "Image Viewer", "PDF Reader");
        documentTypeSelect.setValue("Document Viewer");
        documentTypeSelect.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                viewTitleSignal.value(event.getValue());
            }
        });

        // Display the current title signal value
        Div currentTitleBox = new Div();
        currentTitleBox.getStyle()
            .set("background-color", "#e3f2fd")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin", "1em 0");

        Paragraph titleLabel = new Paragraph("Current Title Signal:");
        titleLabel.getStyle()
            .set("margin", "0 0 0.5em 0")
            .set("font-weight", "bold");

        Paragraph titleDisplay = new Paragraph();
        titleDisplay.getStyle()
            .set("margin", "0")
            .set("font-family", "monospace")
            .set("font-size", "1.1em");
        MissingAPI.bindText(titleDisplay, viewTitleSignal);

        Paragraph browserTitleLabel = new Paragraph("Browser Tab Title:");
        browserTitleLabel.getStyle()
            .set("margin", "1em 0 0.5em 0")
            .set("font-weight", "bold");

        Paragraph browserTitleDisplay = new Paragraph();
        browserTitleDisplay.getStyle()
            .set("margin", "0")
            .set("font-family", "monospace")
            .set("font-size", "1.1em")
            .set("color", "var(--lumo-primary-color)");

        // Compose app name + view title
        Signal<String> fullTitleSignal = viewTitleSignal.map(viewTitle ->
            APP_NAME + " - " + viewTitle
        );
        MissingAPI.bindText(browserTitleDisplay, fullTitleSignal);

        currentTitleBox.add(titleLabel, titleDisplay, browserTitleLabel, browserTitleDisplay);

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle()
            .set("background-color", "#fff3e0")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-top", "1em")
            .set("font-style", "italic");
        infoBox.add(new Paragraph(
            "💡 The browser tab title updates automatically as you change the document type. " +
            "In a real application, parent layouts could bind to this title signal to display " +
            "it in a breadcrumb or page header. The signal composition pattern (APP_NAME + view title) " +
            "ensures consistent title formatting across the application."
        ));

        add(
            title,
            description,
            documentTypeSelect,
            currentTitleBox,
            infoBox
        );
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Bind browser document.title to the full title signal
        UI ui = attachEvent.getUI();
        Signal<String> fullTitleSignal = viewTitleSignal.map(viewTitle ->
            APP_NAME + " - " + viewTitle
        );

        // Update browser title using UI.access
        MissingAPI.bindBrowserTitle(ui, fullTitleSignal);
    }
}
