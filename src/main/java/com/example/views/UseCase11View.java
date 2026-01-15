package com.example.views;

import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Use Case 11: Responsive Layout with Window Size Signal
 *
 * Demonstrates using browser window size as a reactive signal to: - Show/hide
 * components based on screen size - Switch between mobile and desktop layouts -
 * Adjust component configurations for different viewports
 *
 * Key Patterns: - Signal from browser window resize events - Debounced browser
 * event handling - Computed signals for breakpoint detection - Responsive
 * component visibility
 */
@Route(value = "use-case-11", layout = MainLayout.class)
@PageTitle("Use Case 11: Responsive Layout")
@Menu(order = 11, title = "UC 11: Responsive Layout")
@PermitAll
public class UseCase11View extends VerticalLayout {

    public static class WindowSize {
        private final int width;
        private final int height;

        public WindowSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isSmallScreen() {
            return width < 768; // Mobile breakpoint
        }

        public boolean isMediumScreen() {
            return width >= 768 && width < 1024; // Tablet breakpoint
        }

        public boolean isLargeScreen() {
            return width >= 1024; // Desktop breakpoint
        }

        @Override
        public String toString() {
            return width + "×" + height + "px";
        }
    }

    private final WritableSignal<WindowSize> windowSizeSignal = new ValueSignal<>(
            new WindowSize(1024, 768));
    private Registration resizeRegistration;

    public UseCase11View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2(
                "Use Case 11: Responsive Layout with Window Size Signal");

        Paragraph description = new Paragraph(
                "This use case demonstrates responsive UI adaptation based on browser window size. "
                        + "The window size is exposed as a reactive signal, allowing components to show/hide "
                        + "and adjust their layout based on screen breakpoints. Try resizing your browser window!");

        // Window size display
        Div sizeInfoBox = new Div();
        sizeInfoBox.getStyle().set("background-color", "#e3f2fd")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin", "1em 0");

        H3 sizeTitle = new H3("Current Window Size");
        sizeTitle.getStyle().set("margin-top", "0");

        Paragraph sizeDisplay = new Paragraph();
        sizeDisplay.getStyle().set("font-family", "monospace")
                .set("font-size", "1.2em").set("font-weight", "bold");
        Signal<String> sizeText = windowSizeSignal.map(WindowSize::toString);
        sizeDisplay.bindText(sizeText);

        Paragraph breakpointDisplay = new Paragraph();
        Signal<String> breakpointText = windowSizeSignal.map(size -> {
            if (size.isSmallScreen())
                return "📱 Small Screen (Mobile)";
            if (size.isMediumScreen())
                return "💻 Medium Screen (Tablet)";
            return "🖥️ Large Screen (Desktop)";
        });
        breakpointDisplay.bindText(breakpointText);

        sizeInfoBox.add(sizeTitle, sizeDisplay, breakpointDisplay);

        // Mobile-only content
        Div mobileSection = createSection("📱 Mobile-Only Content",
                "This section is only visible on small screens (width < 768px). "
                        + "Perfect for mobile-specific navigation or simplified UI.",
                "#fff3e0");
        Signal<Boolean> isSmallScreen = windowSizeSignal
                .map(WindowSize::isSmallScreen);
        mobileSection.bindVisible(isSmallScreen);

        // Tablet-only content
        Div tabletSection = createSection("💻 Tablet-Only Content",
                "This section appears on medium screens (768px ≤ width < 1024px). "
                        + "Good for tablet-optimized layouts.",
                "#f3e5f5");
        Signal<Boolean> isMediumScreen = windowSizeSignal
                .map(WindowSize::isMediumScreen);
        tabletSection.bindVisible(isMediumScreen);

        // Desktop-only content
        Div desktopSection = createSection("🖥️ Desktop-Only Content",
                "This section is for large screens (width ≥ 1024px). "
                        + "Can show advanced features and detailed information.",
                "#e8f5e9");
        Signal<Boolean> isLargeScreen = windowSizeSignal
                .map(WindowSize::isLargeScreen);
        desktopSection.bindVisible(isLargeScreen);

        // Responsive card grid
        H3 cardGridTitle = new H3("Responsive Card Grid");

        // Use different layouts based on screen size
        Component cardGrid = createResponsiveCardGrid();

        // Test controls
        Div controlsBox = new Div();
        controlsBox.getStyle().set("background-color", "#fff9c4")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em");

        Paragraph controlsLabel = new Paragraph("Simulate Screen Sizes:");
        controlsLabel.getStyle().set("font-weight", "bold");

        HorizontalLayout testButtons = new HorizontalLayout();
        testButtons.setSpacing(true);

        Button mobileBtn = new Button("📱 Mobile (375×667)",
                event -> windowSizeSignal.value(new WindowSize(375, 667)));
        mobileBtn.addThemeName("small");

        Button tabletBtn = new Button("💻 Tablet (768×1024)",
                event -> windowSizeSignal.value(new WindowSize(768, 1024)));
        tabletBtn.addThemeName("small");

        Button desktopBtn = new Button("🖥️ Desktop (1920×1080)",
                event -> windowSizeSignal.value(new WindowSize(1920, 1080)));
        desktopBtn.addThemeName("small");

        Button resetBtn = new Button("↻ Detect Actual Size",
                event -> detectWindowSize());
        resetBtn.addThemeName("small");

        testButtons.add(mobileBtn, tabletBtn, desktopBtn, resetBtn);

        controlsBox.add(controlsLabel, testButtons);

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 The window size signal is updated from browser resize events with debouncing. "
                        + "In a real application, this pattern enables fully responsive UIs without media queries, "
                        + "with the added benefit of reactive state management. Try resizing the browser window "
                        + "or use the test buttons above."));

        add(title, description, sizeInfoBox, mobileSection, tabletSection,
                desktopSection, cardGridTitle, cardGrid, controlsBox, infoBox,
                new SourceCodeLink(getClass()));
    }

    private Div createSection(String title, String content,
            String backgroundColor) {
        Div section = new Div();
        section.getStyle().set("background-color", backgroundColor)
                .set("padding", "1.5em").set("border-radius", "8px")
                .set("margin", "0.5em 0");

        H3 sectionTitle = new H3(title);
        sectionTitle.getStyle().set("margin-top", "0").set("margin-bottom",
                "0.5em");

        Paragraph sectionContent = new Paragraph(content);
        sectionContent.getStyle().set("margin", "0");

        section.add(sectionTitle, sectionContent);
        return section;
    }

    private Component createResponsiveCardGrid() {
        Div gridContainer = new Div();

        // Create sample cards
        for (int i = 1; i <= 6; i++) {
            Div card = new Div();
            card.getStyle().set("background-color", "#ffffff")
                    .set("border", "1px solid #e0e0e0")
                    .set("border-radius", "4px").set("padding", "1em")
                    .set("margin", "0.5em").set("flex", "1 1 200px")
                    .set("min-width", "150px");

            H3 cardTitle = new H3("Card " + i);
            cardTitle.getStyle().set("margin", "0 0 0.5em 0").set("font-size",
                    "1em");

            Paragraph cardContent = new Paragraph("Sample card content");
            cardContent.getStyle().set("margin", "0");

            card.add(cardTitle, cardContent);
            gridContainer.add(card);
        }

        // Set responsive flex layout
        Signal<String> flexDirection = windowSizeSignal.map(size -> {
            if (size.isSmallScreen())
                return "column"; // Stack vertically on mobile
            return "row"; // Row layout on larger screens
        });

        Signal<String> flexWrap = windowSizeSignal
                .map(size -> size.isSmallScreen() ? "nowrap" : "wrap");

        gridContainer.getStyle().set("display", "flex").set("margin", "1em 0");

        gridContainer.getStyle().bind("flex-direction", flexDirection);
        gridContainer.getStyle().bind("flex-wrap", flexWrap);

        return gridContainer;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        UI ui = attachEvent.getUI();

        // Detect initial window size
        detectWindowSize();

        // Set up resize listener with debouncing (simulated here)
        // In real implementation, would use
        // Page.retrieveExtendedClientDetails()
        // or JavaScript execution with window.addEventListener('resize', ...)
        resizeRegistration = ui.getPage()
                .addBrowserWindowResizeListener(event -> {
                    int width = event.getWidth();
                    int height = event.getHeight();
                    windowSizeSignal.value(new WindowSize(width, height));
                });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (resizeRegistration != null) {
            resizeRegistration.remove();
        }
    }

    private void detectWindowSize() {
        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            int width = details.getBodyClientWidth();
            int height = details.getBodyClientHeight();
            windowSizeSignal.value(new WindowSize(width, height));
        });
    }
}
