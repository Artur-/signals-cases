package com.example.views;

import com.example.MissingAPI;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Use Case 14: Async Data Loading with States
 *
 * Demonstrates async operations with loading/success/error states:
 * - Signal with LoadingState<T> (Loading/Success/Error)
 * - Loading spinner while fetching data
 * - Display data on success
 * - Error message with retry button
 * - Optimistic updates with rollback on error
 *
 * Key Patterns:
 * - Async signal updates
 * - Loading state representation
 * - Error handling with retry
 * - Simulated server calls with delays
 */
@Route(value = "use-case-14", layout = MainLayout.class)
@PageTitle("Use Case 14: Async Data Loading")
@Menu(order = 40, title = "UC 14: Async Data Loading")
@PermitAll
public class UseCase14View extends VerticalLayout {

    /**
     * Represents the state of an async operation
     */
    public static class LoadingState<T> {
        private final State state;
        private final T data;
        private final String error;

        public enum State {
            IDLE, LOADING, SUCCESS, ERROR
        }

        private LoadingState(State state, T data, String error) {
            this.state = state;
            this.data = data;
            this.error = error;
        }

        public static <T> LoadingState<T> idle() {
            return new LoadingState<>(State.IDLE, null, null);
        }

        public static <T> LoadingState<T> loading() {
            return new LoadingState<>(State.LOADING, null, null);
        }

        public static <T> LoadingState<T> success(T data) {
            return new LoadingState<>(State.SUCCESS, data, null);
        }

        public static <T> LoadingState<T> error(String message) {
            return new LoadingState<>(State.ERROR, null, message);
        }

        public boolean isIdle() { return state == State.IDLE; }
        public boolean isLoading() { return state == State.LOADING; }
        public boolean isSuccess() { return state == State.SUCCESS; }
        public boolean isError() { return state == State.ERROR; }
        public T getData() { return data; }
        public String getError() { return error; }
    }

    public static class User {
        private final String id;
        private final String name;
        private final String email;

        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    private final WritableSignal<LoadingState<List<User>>> usersSignal =
        new ValueSignal<>(LoadingState.idle());

    private final WritableSignal<Boolean> shouldFailSignal = new ValueSignal<>(false);

    public UseCase14View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 14: Async Data Loading with States");

        Paragraph description = new Paragraph(
            "This use case demonstrates async operations with proper loading/success/error states. " +
            "Click 'Load Users' to fetch data with a simulated delay. Toggle 'Simulate Error' to see error handling. " +
            "The UI reactively shows loading spinners, data on success, or error messages with retry."
        );

        // Controls
        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);

        Button loadButton = new Button("Load Users", event -> loadUsers());
        loadButton.addThemeVariants();

        // Disable load button while loading
        Signal<Boolean> isLoadingSignal = usersSignal.map(LoadingState::isLoading);
        MissingAPI.bindEnabled(loadButton, isLoadingSignal.map(loading -> !loading));

        Button clearButton = new Button("Clear", event ->
            usersSignal.value(LoadingState.idle()));
        clearButton.addThemeName("tertiary");

        Div errorToggle = new Div();
        errorToggle.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5em");

        var checkbox = new com.vaadin.flow.component.checkbox.Checkbox("Simulate Error");
        MissingAPI.bindValue(checkbox, shouldFailSignal);
        errorToggle.add(checkbox);

        controls.add(loadButton, clearButton, errorToggle);

        // State display box
        Div stateBox = new Div();
        stateBox.getStyle()
            .set("border", "2px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "8px")
            .set("padding", "1.5em")
            .set("margin", "1em 0")
            .set("min-height", "300px");

        // Idle state
        Div idleContent = new Div();
        Paragraph idleMessage = new Paragraph("👆 Click 'Load Users' to fetch data from the server");
        idleMessage.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-style", "italic");
        idleContent.add(idleMessage);
        Signal<Boolean> isIdleSignal = usersSignal.map(LoadingState::isIdle);
        MissingAPI.bindVisible(idleContent, isIdleSignal);

        // Loading state
        Div loadingContent = new Div();
        loadingContent.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center")
            .set("gap", "1em");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setWidth("200px");

        Paragraph loadingMessage = new Paragraph("Loading users...");
        loadingMessage.getStyle().set("color", "var(--lumo-primary-color)");

        loadingContent.add(progressBar, loadingMessage);
        MissingAPI.bindVisible(loadingContent, isLoadingSignal);

        // Success state
        Div successContent = new Div();
        H3 successTitle = new H3("Users Loaded Successfully");
        successTitle.getStyle()
            .set("margin-top", "0")
            .set("color", "var(--lumo-success-color)");

        Div userList = new Div();
        userList.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "0.5em");

        // Bind user cards dynamically
        Signal<List<User>> usersDataSignal = usersSignal.map(state ->
            state.isSuccess() ? state.getData() : List.of()
        );
        MissingAPI.bindComponentChildren(userList, usersDataSignal, user -> {
            Div card = new Div();
            card.getStyle()
                .set("background-color", "#f5f5f5")
                .set("padding", "1em")
                .set("border-radius", "4px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "1em");

            Icon userIcon = new Icon(VaadinIcon.USER);
            userIcon.setColor("var(--lumo-primary-color)");

            Div info = new Div();
            Div nameDiv = new Div(user.getName());
            nameDiv.getStyle().set("font-weight", "bold");
            Div emailDiv = new Div(user.getEmail());
            emailDiv.getStyle()
                .set("font-size", "0.9em")
                .set("color", "var(--lumo-secondary-text-color)");
            info.add(nameDiv, emailDiv);

            card.add(userIcon, info);
            return card;
        });

        successContent.add(successTitle, userList);
        Signal<Boolean> isSuccessSignal = usersSignal.map(LoadingState::isSuccess);
        MissingAPI.bindVisible(successContent, isSuccessSignal);

        // Error state
        Div errorContent = new Div();
        errorContent.getStyle()
            .set("background-color", "#ffebee")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("border-left", "4px solid var(--lumo-error-color)");

        H3 errorTitle = new H3("❌ Failed to Load Users");
        errorTitle.getStyle()
            .set("margin-top", "0")
            .set("color", "var(--lumo-error-color)");

        Paragraph errorMessage = new Paragraph();
        Signal<String> errorTextSignal = usersSignal.map(state ->
            state.isError() ? state.getError() : ""
        );
        MissingAPI.bindText(errorMessage, errorTextSignal);

        Button retryButton = new Button("Retry", event -> loadUsers());
        retryButton.addThemeVariants();
        retryButton.setIcon(new Icon(VaadinIcon.REFRESH));

        errorContent.add(errorTitle, errorMessage, retryButton);
        Signal<Boolean> isErrorSignal = usersSignal.map(LoadingState::isError);
        MissingAPI.bindVisible(errorContent, isErrorSignal);

        stateBox.add(idleContent, loadingContent, successContent, errorContent);

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle()
            .set("background-color", "#e0f7fa")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-top", "1em")
            .set("font-style", "italic");
        infoBox.add(new Paragraph(
            "💡 This pattern is essential for real-world applications. The LoadingState<T> wrapper " +
            "provides a type-safe way to represent async operations. In production, this would integrate " +
            "with actual REST/GraphQL calls using CompletableFuture or reactive streams. " +
            "The signal automatically updates the UI as the state transitions: IDLE → LOADING → SUCCESS/ERROR."
        ));

        add(title, description, controls, stateBox, infoBox);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Could auto-load data on attach if desired
    }

    private void loadUsers() {
        // Set loading state immediately
        usersSignal.value(LoadingState.loading());

        // Simulate async server call with delay
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000); // Simulate network delay

                if (shouldFailSignal.value()) {
                    throw new RuntimeException("Server returned 500: Internal Server Error");
                }

                // Simulate fetched data
                return List.of(
                    new User("1", "Alice Johnson", "alice@example.com"),
                    new User("2", "Bob Smith", "bob@example.com"),
                    new User("3", "Charlie Davis", "charlie@example.com"),
                    new User("4", "Diana Martinez", "diana@example.com")
                );
            } catch (InterruptedException e) {
                throw new RuntimeException("Request interrupted");
            }
        }).thenAccept(users -> {
            // Update signal on UI thread
            getUI().ifPresent(ui -> ui.access(() -> {
                usersSignal.value(LoadingState.success(users));
            }));
        }).exceptionally(error -> {
            // Handle error on UI thread
            getUI().ifPresent(ui -> ui.access(() -> {
                usersSignal.value(LoadingState.error(error.getMessage()));
            }));
            return null;
        });
    }
}
