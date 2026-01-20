package com.example.components;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Element;

/**
 * A slider component wrapping HTML5 range input with label and value display.
 * Supports both integer and decimal values.
 */
@Tag("div")
public class Slider extends AbstractSinglePropertyField<Slider, Double> implements HasSize {

    private final Span labelSpan;
    private final Span valueSpan;
    private final Element rangeInput;
    private double min;
    private double max;
    private double step;
    private String valueFormat = "%.0f"; // Default to integer format

    public Slider(String label) {
        this(label, 0.0, 100.0, 1.0);
    }

    public Slider(String label, double min, double max) {
        this(label, min, max, 1.0);
    }

    public Slider(String label, double min, double max, double step) {
        super("value", 0.0, false);

        this.min = min;
        this.max = max;
        this.step = step;

        // Determine if we're dealing with decimals
        if (step < 1.0) {
            // Count decimal places
            String stepStr = String.valueOf(step);
            int decimalPlaces = stepStr.length() - stepStr.indexOf('.') - 1;
            valueFormat = "%." + decimalPlaces + "f";
        }

        getElement().getStyle().set("margin-bottom", "8px");

        // Header with label and value
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        header.getStyle().set("margin-bottom", "4px");

        labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500");

        valueSpan = new Span(formatValue(getValue()));
        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        header.add(labelSpan, valueSpan);

        // Range input element
        rangeInput = new Element("input");
        rangeInput.setAttribute("type", "range");
        rangeInput.setAttribute("min", String.valueOf(min));
        rangeInput.setAttribute("max", String.valueOf(max));
        rangeInput.setAttribute("step", String.valueOf(step));
        rangeInput.setAttribute("value", String.valueOf(getValue()));
        rangeInput.getStyle()
                .set("width", "100%")
                .set("cursor", "pointer");

        // Listen to input events
        rangeInput.addEventListener("input", e -> {
            double newValue = e.getEventData().get("element.value").asDouble();
            setModelValue(newValue, true);
        }).addEventData("element.value");

        Div sliderContainer = new Div();
        sliderContainer.getElement().appendChild(rangeInput);

        getElement().appendChild(header.getElement());
        getElement().appendChild(sliderContainer.getElement());
    }

    @Override
    public void setValue(Double value) {
        super.setValue(value);
        if (value != null) {
            rangeInput.setAttribute("value", String.valueOf(value));
            valueSpan.setText(formatValue(value));
        }
    }

    @Override
    protected void setPresentationValue(Double newPresentationValue) {
        if (newPresentationValue != null) {
            rangeInput.setAttribute("value", String.valueOf(newPresentationValue));
            valueSpan.setText(formatValue(newPresentationValue));
        }
    }

    private String formatValue(Double value) {
        if (value == null) {
            return String.format(java.util.Locale.US, valueFormat, 0.0);
        }
        return String.format(java.util.Locale.US, valueFormat, value);
    }

    public void setLabel(String label) {
        labelSpan.setText(label);
    }

    public String getLabel() {
        return labelSpan.getText();
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
        rangeInput.setAttribute("min", String.valueOf(min));
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
        rangeInput.setAttribute("max", String.valueOf(max));
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
        rangeInput.setAttribute("step", String.valueOf(step));

        // Update format
        if (step < 1.0) {
            String stepStr = String.valueOf(step);
            int decimalPlaces = stepStr.length() - stepStr.indexOf('.') - 1;
            valueFormat = "%." + decimalPlaces + "f";
        } else {
            valueFormat = "%.0f";
        }
    }

    /**
     * Bind this slider to an Integer signal with automatic conversion.
     * Uses a different method name to avoid type erasure clash with bindValue(WritableSignal<Double>).
     */
    public void bind(com.vaadin.signals.WritableSignal<Integer> signal) {
        // Initialize slider with current signal value
        setValue(signal.value().doubleValue());

        // Update signal when slider changes
        addValueChangeListener(e -> {
            if (!e.isFromClient()) return; // Avoid feedback loop
            signal.value(e.getValue().intValue());
        });

        // Update slider when signal changes (from other sources)
        com.vaadin.flow.component.ComponentEffect.effect(this, () -> {
            setValue(signal.value().doubleValue());
        });
    }
}
