package com.example.usecase03;

import jakarta.annotation.security.PermitAll;

import com.example.components.Slider;
import com.example.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.flow.component.ComponentEffect;

@Route(value = "use-case-03", layout = MainLayout.class)
@PageTitle("Use Case 3: Interactive SVG Shape Editor")
@Menu(order = 3, title = "UC 3: Interactive SVG Shape Editor")
@PermitAll
public class UseCase03View extends VerticalLayout {

    // Rectangle signals (green) - top left position
    private final WritableSignal<Integer> rectXSignal = new ValueSignal<>(100);
    private final WritableSignal<Integer> rectYSignal = new ValueSignal<>(50);
    private final WritableSignal<Integer> rectWidthSignal = new ValueSignal<>(150);
    private final WritableSignal<Integer> rectHeightSignal = new ValueSignal<>(80);
    private final WritableSignal<Integer> rectCornerRadiusSignal = new ValueSignal<>(10);
    private final WritableSignal<String> rectFillSignal = new ValueSignal<>("#10b981");
    private final WritableSignal<String> rectStrokeSignal = new ValueSignal<>("#059669");
    private final WritableSignal<Integer> rectStrokeWidthSignal = new ValueSignal<>(2);
    private final WritableSignal<Double> rectOpacitySignal = new ValueSignal<>(1.0);
    private final WritableSignal<Integer> rectRotationSignal = new ValueSignal<>(0);

    // Star signals (orange) - below rectangle
    private final WritableSignal<Integer> starPointsSignal = new ValueSignal<>(5);
    private final WritableSignal<Integer> starSizeSignal = new ValueSignal<>(50);
    private final WritableSignal<Integer> starCxSignal = new ValueSignal<>(175);
    private final WritableSignal<Integer> starCySignal = new ValueSignal<>(300);
    private final WritableSignal<Integer> starRotationSignal = new ValueSignal<>(0);
    private final WritableSignal<String> starFillSignal = new ValueSignal<>("#f59e0b");
    private final WritableSignal<String> starStrokeSignal = new ValueSignal<>("#d97706");
    private final WritableSignal<Integer> starStrokeWidthSignal = new ValueSignal<>(2);
    private final WritableSignal<Double> starOpacitySignal = new ValueSignal<>(1.0);

    // Selected shape tracking
    private final WritableSignal<String> selectedShapeSignal = new ValueSignal<>("rectangle");

    private Element rectElement;
    private Element starElement;

    public UseCase03View() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        H2 title = new H2("Use Case 3: Interactive SVG Shape Editor");
        title.getStyle().set("margin-bottom", "0");

        Paragraph description = new Paragraph(
                "Click on a shape to select it, or use the tabs below. "
                        + "Demonstrates extensive bindAttribute() usage with SVG elements. "
                        + "Each shape is controlled by multiple signals that bind to SVG attributes like position, size, colors, and transforms. "
                        + "(19 writable + 4 computed = 23 signals total).");
        description.getStyle().set("margin-top", "0.5em");

        // Main content: controls on left, canvas on right
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setWidthFull();
        mainContent.setSpacing(true);

        // Left panel: shape selector and controls
        VerticalLayout leftPanel = createLeftPanel();
        leftPanel.setWidth("280px");
        leftPanel.getStyle().set("flex-shrink", "0");

        // Right panel: SVG canvas
        Div svgContainer = createSvgCanvas();
        svgContainer.getStyle().set("flex", "1");

        mainContent.add(leftPanel, svgContainer);

        add(title, description, mainContent);
    }

    private VerticalLayout createLeftPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.setSpacing(true);
        panel.setPadding(false);

        // Shape selector tabs
        Tab rectangleTab = new Tab("🟢 Rectangle");
        Tab starTab = new Tab("🟠 Star");

        Tabs shapeTabs = new Tabs(rectangleTab, starTab);
        shapeTabs.setWidthFull();

        // Controls container that shows only the selected shape's controls
        Div controlsContainer = new Div();
        controlsContainer.setWidthFull();

        // Rectangle controls
        Div rectangleControls = createRectangleControls();

        // Star controls
        Div starControls = createStarControls();

        // Show/hide controls based on selected shape
        ComponentEffect.effect(this, () -> {
            String selected = selectedShapeSignal.value();
            rectangleControls.setVisible("rectangle".equals(selected));
            starControls.setVisible("star".equals(selected));
        });

        controlsContainer.add(rectangleControls, starControls);

        // Sync tabs with signal
        shapeTabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            if (selectedTab == rectangleTab) {
                selectedShapeSignal.value("rectangle");
            } else if (selectedTab == starTab) {
                selectedShapeSignal.value("star");
            }
        });

        // Sync signal with tabs (for when shape is selected by clicking)
        ComponentEffect.effect(this, () -> {
            String selected = selectedShapeSignal.value();
            if ("rectangle".equals(selected)) {
                shapeTabs.setSelectedTab(rectangleTab);
            } else if ("star".equals(selected)) {
                shapeTabs.setSelectedTab(starTab);
            }
        });

        // Reset button
        Button resetButton = new Button("Reset to Defaults", e -> resetAll());
        resetButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        resetButton.setWidthFull();

        panel.add(shapeTabs, controlsContainer, resetButton);
        return panel;
    }

    private Div createSvgCanvas() {
        Div container = new Div();
        container.getStyle()
                .set("border", "2px solid var(--lumo-contrast-10pct)")
                .set("padding", "20px")
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        Element svg = new Element("svg");
        svg.setAttribute("viewBox", "0 0 500 500");
        svg.setAttribute("width", "100%");
        svg.setAttribute("height", "500");
        svg.setAttribute("style", "max-width: 100%;");
        svg.getStyle()
                .set("background", "#fafafa")
                .set("border-radius", "4px");

        // Add background grid for reference
        Element defs = new Element("defs");
        Element pattern = new Element("pattern");
        pattern.setAttribute("id", "grid");
        pattern.setAttribute("width", "20");
        pattern.setAttribute("height", "20");
        pattern.setAttribute("patternUnits", "userSpaceOnUse");

        Element gridPath = new Element("path");
        gridPath.setAttribute("d", "M 20 0 L 0 0 0 20");
        gridPath.setAttribute("fill", "none");
        gridPath.setAttribute("stroke", "#e0e0e0");
        gridPath.setAttribute("stroke-width", "0.5");

        pattern.appendChild(gridPath);
        defs.appendChild(pattern);
        svg.appendChild(defs);

        Element gridRect = new Element("rect");
        gridRect.setAttribute("width", "100%");
        gridRect.setAttribute("height", "100%");
        gridRect.setAttribute("fill", "url(#grid)");
        svg.appendChild(gridRect);

        // Create shapes
        rectElement = createRectangleElement();
        starElement = createStarElement();

        svg.appendChild(rectElement);
        svg.appendChild(starElement);

        // Add click handlers to select shapes
        rectElement.addEventListener("click", e -> {
            selectedShapeSignal.value("rectangle");
        });

        starElement.addEventListener("click", e -> {
            selectedShapeSignal.value("star");
        });

        // Add cursor pointer style for shapes
        rectElement.getStyle().set("cursor", "pointer");
        starElement.getStyle().set("cursor", "pointer");

        container.getElement().appendChild(svg);
        return container;
    }

    private Element createRectangleElement() {
        Element rect = new Element("rect");

        // Bind basic attributes
        rect.bindAttribute("x", rectXSignal.map(String::valueOf));
        rect.bindAttribute("y", rectYSignal.map(String::valueOf));
        rect.bindAttribute("width", rectWidthSignal.map(String::valueOf));
        rect.bindAttribute("height", rectHeightSignal.map(String::valueOf));
        rect.bindAttribute("rx", rectCornerRadiusSignal.map(String::valueOf));
        rect.bindAttribute("fill", rectFillSignal);
        rect.bindAttribute("stroke", rectStrokeSignal);

        // Stroke width increases when selected
        Signal<String> rectStrokeWidthComputed = Signal.computed(() -> {
            int baseWidth = rectStrokeWidthSignal.value();
            boolean isSelected = "rectangle".equals(selectedShapeSignal.value());
            return String.valueOf(isSelected ? baseWidth + 2 : baseWidth);
        });
        rect.bindAttribute("stroke-width", rectStrokeWidthComputed);

        rect.bindAttribute("opacity", rectOpacitySignal.map(String::valueOf));

        // Computed transform attribute (rotate around center)
        Signal<String> rectTransformSignal = Signal.computed(() -> {
            int x = rectXSignal.value();
            int y = rectYSignal.value();
            int w = rectWidthSignal.value();
            int h = rectHeightSignal.value();
            int centerX = x + w / 2;
            int centerY = y + h / 2;
            int rotation = rectRotationSignal.value();
            return String.format("rotate(%d %d %d)", rotation, centerX, centerY);
        });
        rect.bindAttribute("transform", rectTransformSignal);

        rect.setAttribute("filter", "drop-shadow(2px 2px 4px rgba(0,0,0,0.2))");

        return rect;
    }

    private Element createStarElement() {
        Element polygon = new Element("polygon");

        // Computed points attribute (complex calculation) - centered at origin
        Signal<String> starPointsAttrSignal = Signal.computed(() -> {
            int n = starPointsSignal.value();
            int size = starSizeSignal.value();
            return generateStarPoints(n, size, 0, 0); // Generate at origin
        });
        polygon.bindAttribute("points", starPointsAttrSignal);

        // Bind styling attributes
        polygon.bindAttribute("fill", starFillSignal);
        polygon.bindAttribute("stroke", starStrokeSignal);

        // Stroke width increases when selected
        Signal<String> starStrokeWidthComputed = Signal.computed(() -> {
            int baseWidth = starStrokeWidthSignal.value();
            boolean isSelected = "star".equals(selectedShapeSignal.value());
            return String.valueOf(isSelected ? baseWidth + 2 : baseWidth);
        });
        polygon.bindAttribute("stroke-width", starStrokeWidthComputed);

        polygon.bindAttribute("opacity", starOpacitySignal.map(String::valueOf));

        // Computed transform: translate to position, then rotate
        Signal<String> starTransformSignal = Signal.computed(() -> {
            int rotation = starRotationSignal.value();
            int cx = starCxSignal.value();
            int cy = starCySignal.value();
            // Since points are centered at (0,0), translate to position then rotate
            return String.format("translate(%d %d) rotate(%d)", cx, cy, rotation);
        });
        polygon.bindAttribute("transform", starTransformSignal);

        polygon.setAttribute("filter", "drop-shadow(2px 2px 4px rgba(0,0,0,0.2))");

        return polygon;
    }

    private Div createRectangleControls() {
        Div section = new Div();
        section.setWidthFull();

        VerticalLayout fields = new VerticalLayout();
        fields.setSpacing(false);
        fields.setPadding(false);
        fields.setWidthFull();

        // Position section
        Span positionLabel = new Span("Position");
        positionLabel.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500");

        Slider xSlider = new Slider("X", 0, 500);
        xSlider.bind(rectXSignal);
        xSlider.setWidthFull();

        Slider ySlider = new Slider("Y", 0, 500);
        ySlider.bind(rectYSignal);
        ySlider.setWidthFull();

        // Size section
        Span sizeLabel = new Span("Size");
        sizeLabel.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin-top", "8px");

        Slider widthSlider = new Slider("Width", 50, 250);
        widthSlider.bind(rectWidthSignal);
        widthSlider.setWidthFull();

        Slider heightSlider = new Slider("Height", 30, 150);
        heightSlider.bind(rectHeightSignal);
        heightSlider.setWidthFull();

        Slider cornerRadiusSlider = new Slider("Corner Radius", 0, 50);
        cornerRadiusSlider.bind(rectCornerRadiusSignal);
        cornerRadiusSlider.setWidthFull();

        // Appearance section
        Span appearanceLabel = new Span("Appearance");
        appearanceLabel.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin-top", "8px");

        ComboBox<String> fillColorField = createColorPicker("Fill");
        fillColorField.bindValue(rectFillSignal);
        fillColorField.setWidthFull();

        ComboBox<String> strokeColorField = createColorPicker("Stroke");
        strokeColorField.bindValue(rectStrokeSignal);
        strokeColorField.setWidthFull();

        Slider opacitySlider = new Slider("Opacity", 0.0, 1.0, 0.1);
        opacitySlider.bindValue(rectOpacitySignal);
        opacitySlider.setWidthFull();

        // Transform section
        Span transformLabel = new Span("Transform");
        transformLabel.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin-top", "8px");

        Slider rotationSlider = new Slider("Rotation", 0, 360);
        rotationSlider.bind(rectRotationSignal);
        rotationSlider.setWidthFull();

        fields.add(positionLabel, xSlider, ySlider, sizeLabel, widthSlider, heightSlider,
                cornerRadiusSlider, appearanceLabel, fillColorField, strokeColorField, opacitySlider,
                transformLabel, rotationSlider);

        section.add(fields);
        return section;
    }

    private Div createStarControls() {
        Div section = new Div();
        section.setWidthFull();

        VerticalLayout fields = new VerticalLayout();
        fields.setSpacing(false);
        fields.setPadding(false);
        fields.setWidthFull();

        // Position section
        Span positionLabel = new Span("Position");
        positionLabel.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500");

        Slider cxSlider = new Slider("Center X", 0, 500);
        cxSlider.bind(starCxSignal);
        cxSlider.setWidthFull();

        Slider cySlider = new Slider("Center Y", 0, 500);
        cySlider.bind(starCySignal);
        cySlider.setWidthFull();

        // Shape section
        Span shapeLabel = new Span("Shape");
        shapeLabel.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin-top", "8px");

        Slider pointsSlider = new Slider("Points", 3, 10);
        pointsSlider.bind(starPointsSignal);
        pointsSlider.setWidthFull();

        Slider sizeSlider = new Slider("Size", 30, 80);
        sizeSlider.bind(starSizeSignal);
        sizeSlider.setWidthFull();

        // Appearance section
        Span appearanceLabel = new Span("Appearance");
        appearanceLabel.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin-top", "8px");

        ComboBox<String> fillColorField = createColorPicker("Fill");
        fillColorField.bindValue(starFillSignal);
        fillColorField.setWidthFull();

        ComboBox<String> strokeColorField = createColorPicker("Stroke");
        strokeColorField.bindValue(starStrokeSignal);
        strokeColorField.setWidthFull();

        Slider opacitySlider = new Slider("Opacity", 0.0, 1.0, 0.1);
        opacitySlider.bindValue(starOpacitySignal);
        opacitySlider.setWidthFull();

        // Transform section
        Span transformLabel = new Span("Transform");
        transformLabel.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin-top", "8px");

        Slider rotationSlider = new Slider("Rotation", 0, 360);
        rotationSlider.bind(starRotationSignal);
        rotationSlider.setWidthFull();

        fields.add(positionLabel, cxSlider, cySlider, shapeLabel, pointsSlider, sizeSlider,
                appearanceLabel, fillColorField, strokeColorField, opacitySlider, transformLabel, rotationSlider);

        section.add(fields);
        return section;
    }

    /**
     * Create a color picker ComboBox with 10 predefined colors.
     */
    private ComboBox<String> createColorPicker(String label) {
        ComboBox<String> combo = new ComboBox<>(label);
        combo.setItems(
                "#3b82f6", // Blue
                "#ef4444", // Red
                "#10b981", // Green
                "#f59e0b", // Orange
                "#8b5cf6", // Purple
                "#ec4899", // Pink
                "#14b8a6", // Teal
                "#f97316", // Deep Orange
                "#6366f1", // Indigo
                "#84cc16"  // Lime
        );

        // Custom renderer to show color swatch
        combo.setRenderer(new com.vaadin.flow.data.renderer.ComponentRenderer<>(color -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(HorizontalLayout.Alignment.CENTER);
            layout.setSpacing(true);

            Div swatch = new Div();
            swatch.getStyle()
                    .set("width", "20px")
                    .set("height", "20px")
                    .set("background-color", color)
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("border-radius", "4px");

            Span text = new Span(color);
            text.getStyle().set("font-size", "var(--lumo-font-size-s)");

            layout.add(swatch, text);
            return layout;
        }));

        return combo;
    }

    /**
     * Generate SVG polygon points for a star shape.
     *
     * @param numPoints Number of points on the star (3-10)
     * @param size Outer radius of the star
     * @param cx Center X coordinate
     * @param cy Center Y coordinate
     * @return SVG points attribute string
     */
    private String generateStarPoints(int numPoints, int size, int cx, int cy) {
        StringBuilder points = new StringBuilder();
        double angleStep = Math.PI / numPoints;
        int outerRadius = size;
        int innerRadius = size / 2;

        for (int i = 0; i < numPoints * 2; i++) {
            double angle = i * angleStep - Math.PI / 2;
            int radius = (i % 2 == 0) ? outerRadius : innerRadius;
            int x = cx + (int) (radius * Math.cos(angle));
            int y = cy + (int) (radius * Math.sin(angle));
            if (i > 0)
                points.append(" ");
            points.append(x).append(",").append(y);
        }

        return points.toString();
    }

    private void resetAll() {
        // Reset rectangle
        rectXSignal.value(100);
        rectYSignal.value(50);
        rectWidthSignal.value(150);
        rectHeightSignal.value(80);
        rectCornerRadiusSignal.value(10);
        rectFillSignal.value("#10b981");
        rectStrokeSignal.value("#059669");
        rectStrokeWidthSignal.value(2);
        rectOpacitySignal.value(1.0);
        rectRotationSignal.value(0);

        // Reset star
        starPointsSignal.value(5);
        starSizeSignal.value(50);
        starCxSignal.value(175);
        starCySignal.value(300);
        starRotationSignal.value(0);
        starFillSignal.value("#f59e0b");
        starStrokeSignal.value("#d97706");
        starStrokeWidthSignal.value(2);
        starOpacitySignal.value(1.0);
    }
}
