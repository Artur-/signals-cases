package com.example.usecase21;

import jakarta.annotation.security.PermitAll;

import java.util.Locale;

import com.example.preferences.UserPreferences;
import com.example.views.MainLayout;
import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;

@Route(value = "use-case-21", layout = MainLayout.class)
@PageTitle("Use Case 21: Signals-Based i18n")
@Menu(order = 21, title = "UC 21: Signals-Based i18n")
@PermitAll
public class UseCase21View extends VerticalLayout {

    private final TranslationHelper t;

    public UseCase21View(UserPreferences userPreferences) {
        setSpacing(true);
        setPadding(true);

        t = new TranslationHelper(userPreferences.localeSignal());

        // Title and description
        H2 title = new H2();
        title.bindText(t.t("uc21.title"));

        Paragraph description = new Paragraph();
        description.bindText(t.t("uc21.description"));

        add(title, description);

        // Welcome section
        add(createWelcomeSection());

        // How It Works section
        add(createHowItWorksSection());

        // Sample Form section
        add(createSampleFormSection());

        // Status section
        add(createStatusSection());
    }

    private Div createWelcomeSection() {
        Div card = createCard();

        H3 heading = new H3();
        heading.bindText(t.t("uc21.welcome.heading"));

        Paragraph text = new Paragraph();
        text.bindText(t.t("uc21.welcome.text"));

        card.add(heading, text);
        return card;
    }

    private Div createHowItWorksSection() {
        Div card = createCard();

        H3 heading = new H3();
        heading.bindText(t.t("uc21.howItWorks.heading"));

        Paragraph step1 = new Paragraph();
        step1.bindText(t.t("uc21.howItWorks.step1"));

        Paragraph step2 = new Paragraph();
        step2.bindText(t.t("uc21.howItWorks.step2"));

        Paragraph step3 = new Paragraph();
        step3.bindText(t.t("uc21.howItWorks.step3"));

        Paragraph step4 = new Paragraph();
        step4.bindText(t.t("uc21.howItWorks.step4"));

        card.add(heading, step1, step2, step3, step4);
        return card;
    }

    private Div createSampleFormSection() {
        Div card = createCard();

        H3 heading = new H3();
        heading.bindText(t.t("uc21.sampleForm.heading"));

        // Name field
        TextField nameField = new TextField();
        nameField.setWidthFull();
        ComponentEffect.bind(nameField, t.t("uc21.sampleForm.nameLabel"),
                (field, label) -> field.setLabel(label));
        ComponentEffect.bind(nameField, t.t("uc21.sampleForm.namePlaceholder"),
                (field, placeholder) -> field.setPlaceholder(placeholder));

        // Email field
        EmailField emailField = new EmailField();
        emailField.setWidthFull();
        ComponentEffect.bind(emailField, t.t("uc21.sampleForm.emailLabel"),
                (field, label) -> field.setLabel(label));
        ComponentEffect.bind(emailField, t.t("uc21.sampleForm.emailPlaceholder"),
                (field, placeholder) -> field.setPlaceholder(placeholder));

        // Buttons
        Button submitButton = new Button();
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.bindText(t.t("uc21.sampleForm.submitButton"));

        Button cancelButton = new Button();
        cancelButton.bindText(t.t("uc21.sampleForm.cancelButton"));

        HorizontalLayout buttonLayout = new HorizontalLayout(submitButton, cancelButton);
        buttonLayout.setSpacing(true);

        VerticalLayout formLayout = new VerticalLayout(nameField, emailField, buttonLayout);
        formLayout.setSpacing(true);
        formLayout.setPadding(false);
        formLayout.setMaxWidth("400px");

        card.add(heading, formLayout);
        return card;
    }

    private Div createStatusSection() {
        Div card = createCard();

        H3 heading = new H3();
        heading.bindText(t.t("uc21.status.heading"));

        // Language display
        HorizontalLayout languageRow = new HorizontalLayout();
        languageRow.setAlignItems(Alignment.CENTER);
        languageRow.setSpacing(true);

        Span languageLabel = new Span();
        languageLabel.bindText(t.t("uc21.status.language").map(s -> s + ":"));
        languageLabel.getStyle().set("font-weight", "bold");

        Span languageValue = new Span();
        Signal<String> languageNameSignal = Signal.computed(() -> {
            Locale locale = t.getLocaleSignal().value();
            return locale.getDisplayLanguage(locale);
        });
        languageValue.bindText(languageNameSignal);

        languageRow.add(languageLabel, languageValue);

        // Locale code display
        HorizontalLayout localeRow = new HorizontalLayout();
        localeRow.setAlignItems(Alignment.CENTER);
        localeRow.setSpacing(true);

        Span localeLabel = new Span();
        localeLabel.bindText(t.t("uc21.status.locale").map(s -> s + ":"));
        localeLabel.getStyle().set("font-weight", "bold");

        Span localeValue = new Span();
        localeValue.bindText(t.getLocaleSignal().map(Locale::toLanguageTag));
        localeValue.getStyle()
                .set("font-family", "monospace")
                .set("background-color", "var(--lumo-contrast-10pct)")
                .set("padding", "0.25em 0.5em")
                .set("border-radius", "4px");

        localeRow.add(localeLabel, localeValue);

        card.add(heading, languageRow, localeRow);
        return card;
    }

    private Div createCard() {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "8px")
                .set("padding", "1.5em")
                .set("margin-bottom", "1em");
        return card;
    }
}
