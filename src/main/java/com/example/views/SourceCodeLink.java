package com.example.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Reusable component that displays a link to the source code on GitHub.
 */
public class SourceCodeLink extends Div {

    private static final String GITHUB_BASE_URL = "https://github.com/vaadin/signals-cases/tree/main/src/main/java/com/example/views/";

    /**
     * Creates a source code link for the given view class.
     *
     * @param viewClass the view class to create a link for
     */
    public SourceCodeLink(Class<?> viewClass) {
        this(viewClass.getSimpleName() + ".java");
    }

    /**
     * Creates a source code link for the given filename.
     *
     * @param filename the filename (e.g., "MUC01View.java")
     */
    public SourceCodeLink(String filename) {
        getStyle().set("background-color", "#f5f5f5")
                .set("padding", "0.5em 0.75em")
                .set("border-radius", "4px")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "0.5em")
                .set("margin-top", "1em");

        Icon icon = VaadinIcon.CODE.create();
        icon.setSize("16px");
        icon.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Anchor link = new Anchor(GITHUB_BASE_URL + filename, "View source code");
        link.setTarget("_blank");
        link.getStyle().set("color", "var(--lumo-primary-text-color)")
                .set("text-decoration", "none")
                .set("font-size", "var(--lumo-font-size-s)");

        add(icon, link);
    }
}
