package com.example.usecase11;

import com.example.views.MainLayout;

import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Use Case 11: Responsive Layout with Container Size Signal
 *
 * Demonstrates responsive content inside a resizable container using a split panel.
 * The content adapts to the available width as you drag the splitter, showing
 * how components can be responsive within any container, not just the window.
 *
 * Key Patterns:
 * - ResizeObserver for container size tracking
 * - Signal from container resize events
 * - Computed signals for breakpoint detection
 * - Responsive component visibility and layout within a fixed container
 * - Split panel with draggable divider
 */
@Route(value = "use-case-11", layout = MainLayout.class)
@PageTitle("Use Case 11: Responsive Layout")
@Menu(order = 11, title = "UC 11: Responsive Layout")
@PermitAll
public class UseCase11View extends VerticalLayout {

    public static class ContainerSize {
        private final int width;
        private final int height;

        public ContainerSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isSmall() {
            return width < 400; // Small breakpoint
        }

        public boolean isMedium() {
            return width >= 400 && width < 700; // Medium breakpoint
        }

        public boolean isLarge() {
            return width >= 700; // Large breakpoint
        }

        @Override
        public String toString() {
            return width + "×" + height + "px";
        }
    }

    private final WritableSignal<ContainerSize> containerSizeSignal = new ValueSignal<>(
            new ContainerSize(600, 400));
    private Div responsiveContent;

    public UseCase11View() {
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        H2 title = new H2("Use Case 11: Responsive Content in Resizable Container");

        Paragraph description = new Paragraph(
                "This use case demonstrates responsive content inside a resizable container. "
                        + "Drag the splitter left and right to resize the content area. "
                        + "The content adapts to the available width, showing different layouts at different sizes. "
                        + "This uses ResizeObserver to track container size changes.");

        // Create split layout
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(60); // Start at 60% for the primary content

        // Left side: Static info panel
        Div infoPanel = createInfoPanel();

        // Right side: Responsive content area
        responsiveContent = createResponsiveContent();

        splitLayout.addToPrimary(responsiveContent);
        splitLayout.addToSecondary(infoPanel);

        // Info box
        Div tipBox = new Div();
        tipBox.getStyle()
                .set("background-color", "#e0f7fa")
                .set("padding", "1em")
                .set("border-radius", "4px")
                .set("margin-bottom", "1em")
                .set("font-style", "italic");
        tipBox.add(new Paragraph(
                "💡 Drag the splitter to resize the content area. " +
                "The responsive content will adapt to different widths, " +
                "showing mobile layout (< 400px), tablet layout (400-700px), or desktop layout (≥ 700px). " +
                "This demonstrates container queries with Signals using ResizeObserver."));

        add(title, description, tipBox, splitLayout);
    }

    private Div createInfoPanel() {
        Div panel = new Div();
        panel.getStyle()
                .set("padding", "1em")
                .set("background-color", "#f5f5f5")
                .set("height", "100%")
                .set("overflow-y", "auto");

        H3 title = new H3("Info Panel");
        title.getStyle().set("margin-top", "0");

        Paragraph info = new Paragraph(
                "This is a static side panel. The main content on the left adapts " +
                "to its container size as you drag the splitter.");

        H3 statsTitle = new H3("Current Size");

        // Container size display
        Div sizeDisplay = new Div();
        sizeDisplay.getStyle()
                .set("background-color", "#ffffff")
                .set("padding", "1em")
                .set("border-radius", "4px")
                .set("margin", "1em 0");

        Paragraph widthPara = new Paragraph();
        widthPara.getStyle().set("font-family", "monospace").set("margin", "0.25em 0");
        Signal<String> widthText = containerSizeSignal.map(size -> "Width: " + size.getWidth() + "px");
        widthPara.bindText(widthText);

        Paragraph heightPara = new Paragraph();
        heightPara.getStyle().set("font-family", "monospace").set("margin", "0.25em 0");
        Signal<String> heightText = containerSizeSignal.map(size -> "Height: " + size.getHeight() + "px");
        heightPara.bindText(heightText);

        Paragraph breakpointPara = new Paragraph();
        breakpointPara.getStyle().set("font-weight", "bold").set("margin", "0.5em 0 0 0");
        Signal<String> breakpointText = containerSizeSignal.map(size -> {
            if (size.isSmall()) return "📱 Small (< 400px)";
            if (size.isMedium()) return "💻 Medium (400-700px)";
            return "🖥️ Large (≥ 700px)";
        });
        breakpointPara.bindText(breakpointText);

        sizeDisplay.add(widthPara, heightPara, breakpointPara);

        panel.add(title, info, statsTitle, sizeDisplay);
        return panel;
    }

    private Div createResponsiveContent() {
        Div container = new Div();
        container.getStyle()
                .set("padding", "1em")
                .set("height", "100%")
                .set("overflow-y", "auto")
                .set("background-color", "#ffffff");

        // Small width content
        Div smallContent = createSection(
                "📱 Small Width Layout",
                "This is the mobile view (width < 400px). Navigation is stacked vertically, " +
                "and complex UI elements are simplified or hidden.",
                "#fff3e0");
        Signal<Boolean> isSmall = containerSizeSignal.map(ContainerSize::isSmall);
        smallContent.bindVisible(isSmall);

        // Medium width content
        Div mediumContent = createSection(
                "💻 Medium Width Layout",
                "This is the tablet view (400px ≤ width < 700px). Navigation can be horizontal, " +
                "and more content is visible with a balanced layout.",
                "#f3e5f5");
        Signal<Boolean> isMedium = containerSizeSignal.map(ContainerSize::isMedium);
        mediumContent.bindVisible(isMedium);

        // Large width content
        Div largeContent = createSection(
                "🖥️ Large Width Layout",
                "This is the desktop view (width ≥ 700px). All features are visible, " +
                "with multi-column layouts and detailed information.",
                "#e8f5e9");
        Signal<Boolean> isLarge = containerSizeSignal.map(ContainerSize::isLarge);
        largeContent.bindVisible(isLarge);

        // Responsive card grid
        H3 cardGridTitle = new H3("Responsive Card Grid");
        Component cardGrid = createResponsiveCardGrid();

        container.add(smallContent, mediumContent, largeContent, cardGridTitle, cardGrid);
        return container;
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
            card.getStyle()
                    .set("background-color", "#f9f9f9")
                    .set("border", "1px solid #e0e0e0")
                    .set("border-radius", "4px")
                    .set("padding", "1em")
                    .set("margin", "0.5em")
                    .set("flex", "1 1 150px")
                    .set("min-width", "120px");

            H3 cardTitle = new H3("Card " + i);
            cardTitle.getStyle().set("margin", "0 0 0.5em 0").set("font-size", "1em");

            Paragraph cardContent = new Paragraph("Content item " + i);
            cardContent.getStyle().set("margin", "0").set("font-size", "0.875em");

            card.add(cardTitle, cardContent);
            gridContainer.add(card);
        }

        // Set responsive flex layout based on container size
        Signal<String> flexDirection = containerSizeSignal.map(size -> {
            if (size.isSmall())
                return "column"; // Stack vertically in small containers
            return "row"; // Row layout in larger containers
        });

        Signal<String> flexWrap = containerSizeSignal
                .map(size -> size.isSmall() ? "nowrap" : "wrap");

        gridContainer.getStyle().set("display", "flex").set("margin", "1em 0");

        gridContainer.getStyle().bind("flex-direction", flexDirection);
        gridContainer.getStyle().bind("flex-wrap", flexWrap);

        return gridContainer;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Set up ResizeObserver to monitor the responsive content area
        String script = """
                const targetElement = $1;
                const resizeObserver = new ResizeObserver(entries => {
                    for (let entry of entries) {
                        const width = Math.floor(entry.contentRect.width);
                        const height = Math.floor(entry.contentRect.height);
                        $0.$server.updateContainerSize(width, height);
                    }
                });
                resizeObserver.observe(targetElement);

                // Store observer for cleanup
                targetElement._resizeObserver = resizeObserver;

                // Report initial size
                const rect = targetElement.getBoundingClientRect();
                $0.$server.updateContainerSize(Math.floor(rect.width), Math.floor(rect.height));
                """;

        getElement().executeJs(script, getElement(), responsiveContent);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        // Clean up ResizeObserver
        String cleanupScript = """
                const targetElement = $0;
                if (targetElement && targetElement._resizeObserver) {
                    targetElement._resizeObserver.disconnect();
                    delete targetElement._resizeObserver;
                }
                """;

        getElement().executeJs(cleanupScript, responsiveContent);
    }

    /**
     * Called from JavaScript when the container is resized
     */
    @com.vaadin.flow.component.ClientCallable
    public void updateContainerSize(int width, int height) {
        containerSizeSignal.value(new ContainerSize(width, height));
    }
}
