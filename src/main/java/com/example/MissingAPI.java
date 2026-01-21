package com.example;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Temporary helper class providing static methods for Signal-based component
 * bindings until the official API is implemented in Vaadin.
 */
public class MissingAPI {

    /**
     * Binds a Grid's items to a Signal containing a List.
     */
    public static <T> void bindItems(Grid<T> grid, Signal<List<T>> signal) {
        ComponentEffect.bind(grid, signal, (g, items) -> {
            if (items != null) {
                g.setItems(items);
            } else {
                g.setItems(List.of());
            }
        });
    }

    /**
     * Binds a Grid's items to a ListSignal. Registers dependencies on all
     * individual ValueSignals within the ListSignal by reading each one, so the
     * Grid updates when any item changes.
     */
    public static <T> void bindItems(Grid<T> grid, ListSignal<T> listSignal) {
        ComponentEffect.effect(grid, () -> {
            List<ValueSignal<T>> signals = listSignal.value();
            // Read each individual signal to register dependency
            List<T> items = signals.stream().map(ValueSignal::value).toList();
            grid.setItems(items);
        });
    }

    /**
     * Binds a ComboBox's items to a Signal containing a List.
     */
    public static <T> void bindItems(
            com.vaadin.flow.component.combobox.ComboBox<T> comboBox,
            Signal<List<T>> signal) {
        ComponentEffect.bind(comboBox, signal, (cb, items) -> {
            if (items != null) {
                cb.setItems(items);
            } else {
                cb.setItems(List.of());
            }
        });
    }

    /**
     * Binds a component's children dynamically based on a Signal containing a
     * List.
     */
    public static <T> void bindChildren(Component container,
            Signal<List<T>> signal, Function<T, Component> mapper) {
        ComponentEffect.bind(container, signal, (c, items) -> {
            c.getElement().removeAllChildren();
            if (items != null) {
                items.stream().map(mapper).forEach(child -> c.getElement()
                        .appendChild(child.getElement()));
            }
        });
    }

    /**
     * Binds a component's children dynamically when the signal already contains
     * Component instances.
     */
    public static <T extends Component> void bindChildren(Component container,
            Signal<List<T>> signal) {
        ComponentEffect.bind(container, signal, (c, items) -> {
            c.getElement().removeAllChildren();
            if (items != null) {
                items.forEach(child -> c.getElement()
                        .appendChild(child.getElement()));
            }
        });
    }

    /**
     * Binds a component's children using ComponentProvider pattern (for
     * advanced use cases).
     */
    public static <T> void bindComponentChildren(Component container,
            Signal<List<T>> signal, Function<T, Component> mapper) {
        bindChildren(container, signal, mapper);
    }

    /**
     * Binds the browser document title to a Signal. The UI is used to get the
     * page and execute JavaScript to update document.title.
     */
    public static void bindBrowserTitle(com.vaadin.flow.component.UI ui,
            Signal<String> signal) {
        ComponentEffect.effect(ui, () -> {
            String title = signal.value();
            if (title != null) {
                ui.getPage().setTitle(title);
            }
        });
    }

    /**
     * Binds a component's invalid state to a Signal containing a boolean value.
     */
    public static void bindInvalid(
            com.vaadin.flow.component.HasValidation component,
            Signal<Boolean> signal) {
        component.setManualValidation(true);
        ComponentEffect.bind((Component) component, signal,
                (c, invalid) -> ((com.vaadin.flow.component.HasValidation) c)
                        .setInvalid(invalid));
    }

    /**
     * Binds a Binder field to a Signal. May be replaced by official API
     * <code>public Signal Binder::bind(HasValue field, String property)</code>.
     *
     * @param binder
     *            the Binder instance (unused but kept for future signature
     *            compatibility)
     * @param field
     *            the field to bind
     * @param property
     *            the property name (unused but kept for future signature
     *            compatibility)
     * @param <T>
     *            the field value type
     */
    public static <T> Signal<T> binderBind(Binder<?> binder,
            HasValue<?, T> field, String property) {
        WritableSignal<T> signal = new ValueSignal<>(field.getValue());
        field.bindValue(signal);
        return signal;
    }

    /**
     * Validates a Binder binding inside a ComponentEffect, triggered by the
     * given Signals. Workaround for future official API that may execute
     * validator always inside as a Signal effect.
     *
     * @param binder
     *            the Binder instance
     * @param effectOwner
     *            the Component to own the effect
     * @param validatedBindingProperty
     *            the property name of the binding to validate
     * @param triggerSignals
     *            the Signals that trigger the validation when their value
     *            changes
     */
    public static void binderValidateInEffect(Binder<?> binder,
            Component effectOwner, String validatedBindingProperty,
            Signal<?>... triggerSignals) {
        ComponentEffect.effect(effectOwner, () -> {
            Stream.of(triggerSignals).forEach(Signal::value);
            binder.getBinding(validatedBindingProperty)
                    .ifPresent(Binder.Binding::validate);
        });
    }
}
