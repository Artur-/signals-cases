# Vaadin Signals Guide

A comprehensive guide to using Vaadin Signals for reactive UI development.

## Overview

Signals are reactive primitives that automatically track dependencies and update the UI when values change. They eliminate the need for manual `ui.access()` calls and event listeners for most UI updates.

**Key Benefits:**
- Automatic UI updates when data changes
- Thread-safe by default
- Declarative reactive patterns
- No manual dependency tracking needed

## Signal Packages

Signals are organized into two packages based on their scope:

**Local Signals** (`com.vaadin.signals.local`):
- `ValueSignal<T>` - For UI-scoped state (form fields, component state)

**Shared Signals** (`com.vaadin.signals.shared`):
- `SharedValueSignal<T>` - For application/session-scoped single values
- `SharedListSignal<T>` - For shared lists
- `SharedMapSignal<V>` - For shared key-value maps

## Signal Types

### ValueSignal<T> (Local)

**Package:** `com.vaadin.signals.local`

**Use for:** Local UI state - form fields, component state, view-specific data.

**Key characteristics:**
- Scoped to a single UI/view
- Compares values using `.equals()`
- Only notifies subscribers when the value actually changes
- Best for simple data types (String, Integer, Boolean, etc.)
- Good for immutable records and value objects

**Important methods:**
```java
import com.vaadin.signals.local.ValueSignal;

ValueSignal<String> name = new ValueSignal<>("Alice");

// Get current value
String current = name.value();

// Set new value (notifies if different)
name.value("Bob");

// Subscribe to changes
name.subscribe(() -> {
    System.out.println("Name changed to: " + name.value());
});
```

**Example:**
```java
import com.vaadin.signals.local.ValueSignal;

private final ValueSignal<String> usernameSignal = new ValueSignal<>("");
private final ValueSignal<Boolean> isLoadingSignal = new ValueSignal<>(false);

// Bind to UI
TextField username = new TextField();
username.bindValue(usernameSignal);

Button submitButton = new Button("Submit");
submitButton.bindEnabled(isLoadingSignal.map(loading -> !loading));
```

### SharedValueSignal<T>

**Package:** `com.vaadin.signals.shared`

**Use for:** Application or session-scoped state that needs to be shared between users or components.

**Key characteristics:**
- Can be shared across multiple UIs/sessions
- Thread-safe for concurrent access
- Compares values using `.equals()`
- Perfect for global settings, shared counters, application state

**Important methods:**
```java
import com.vaadin.signals.shared.SharedValueSignal;

SharedValueSignal<String> globalMessage = new SharedValueSignal<>("");

// Get current value
String current = globalMessage.value();

// Set new value (all subscribers notified)
globalMessage.value("System maintenance in 5 minutes");

// Subscribe to changes
globalMessage.subscribe(() -> {
    System.out.println("Message: " + globalMessage.value());
});
```

**Example - Shared counter:**
```java
import com.vaadin.signals.shared.SharedValueSignal;

@Service
public class VisitorCounterService {
    private final SharedValueSignal<Integer> visitorCount = new SharedValueSignal<>(0);

    public SharedValueSignal<Integer> getVisitorCount() {
        return visitorCount;
    }

    public void incrementVisitors() {
        visitorCount.value(visitorCount.value() + 1);
    }
}
```

### SharedListSignal<T>

**Package:** `com.vaadin.signals.shared`

**Use for:** Managing shared lists of items with granular reactivity.

**Key characteristics:**
- Stores `List<ValueSignal<T>>` internally
- Each item is individually reactive
- Structural changes (add/remove) notify the list
- Item mutations notify individual signals
- Perfect for shared collections, chat messages, collaborative lists

**Important methods:**
```java
import com.vaadin.signals.shared.SharedListSignal;

SharedListSignal<Task> tasks = new SharedListSignal<>(Task.class);

// Add items
tasks.insertLast(new Task("Write docs"));
tasks.insertFirst(new Task("Review PR"));
tasks.insert(1, new Task("Fix bug"));

// Remove items
tasks.remove(taskSignal); // Remove by signal reference
tasks.value().clear(); // Clear all

// Access as list of signals
List<ValueSignal<Task>> signals = tasks.value();

// Access individual values
Task firstTask = tasks.value().get(0).value();

// Update individual items
tasks.value().get(0).value(updatedTask);
```

**Example with mutations:**
```java
import com.vaadin.signals.shared.SharedListSignal;

SharedListSignal<Task> tasks = new SharedListSignal<>(Task.class);

// Add tasks
tasks.insertLast(new Task("1", "First", "Description", TODO, false));
tasks.insertLast(new Task("2", "Second", "Description", TODO, false));

// Update a specific task
tasks.value().stream()
    .filter(sig -> sig.value().id().equals("1"))
    .findFirst()
    .ifPresent(sig -> sig.value(sig.value().withCompleted(true)));
```

### SharedMapSignal<V>

**Package:** `com.vaadin.signals.shared`

**Use for:** Managing shared String key-value pairs with reactive updates.

**Key characteristics:**
- Keys are always String type
- Stores `Map<String, ValueSignal<V>>` internally
- Each value is individually reactive
- Map structure changes notify subscribers
- Value mutations notify individual signals
- Perfect for user sessions, settings, caches

**Important methods:**
```java
import com.vaadin.signals.shared.SharedMapSignal;

SharedMapSignal<User> users = new SharedMapSignal<>(User.class);

// Add/update entries
users.put("user1", new User("Alice"));
users.put("user2", new User("Bob"));

// Get value signal for a key
ValueSignal<User> userSignal = users.get("user1");
if (userSignal != null) {
    User user = userSignal.value();
}

// Remove entries
users.remove("user1");

// Access as map of signals
Map<String, ValueSignal<User>> map = users.value();

// Check if key exists
boolean hasUser = users.value().containsKey("user1");

// Update individual values
ValueSignal<User> sig = users.get("user1");
if (sig != null) {
    sig.value(new User("Alice Updated")); // Notifies!
}
```

**Example - Active users tracking:**
```java
import com.vaadin.signals.shared.SharedMapSignal;

SharedMapSignal<UserInfo> activeUsers = new SharedMapSignal<>(UserInfo.class);

// User joins
activeUsers.put(sessionId, new UserInfo(username, "lobby"));

// User changes location
ValueSignal<UserInfo> userSig = activeUsers.get(sessionId);
if (userSig != null) {
    userSig.value(userSig.value().withLocation("game-room"));
}

// User leaves
activeUsers.remove(sessionId);

// Bind to UI
Span userCount = new Span();
userCount.bindText(activeUsers.map(map -> "Users: " + map.size()));
```

## Computed Signals

**Use for:** Deriving values from other signals.

**Key characteristics:**
- Automatically tracks dependencies
- Re-evaluates when any dependency changes
- Lazy evaluation (only computes when accessed)
- Can depend on multiple signals

**Creating computed signals:**

Computed signals are created automatically when you use `.map()` or explicitly with `Signal.computed()`:

**Using .map() for single-signal transformations:**
```java
import com.vaadin.signals.local.ValueSignal;

ValueSignal<String> name = new ValueSignal<>("alice");

Signal<String> upperName = name.map(String::toUpperCase);
// upperName.value() is "ALICE"

name.value("bob");
// upperName.value() is now "BOB"
```

**Using Signal.computed() for multi-signal dependencies:**
```java
import com.vaadin.signals.local.ValueSignal;

ValueSignal<Integer> width = new ValueSignal<>(10);
ValueSignal<Integer> height = new ValueSignal<>(5);

Signal<Integer> area = Signal.computed(() ->
    width.value() * height.value()
);

// area automatically updates when width or height changes
width.value(20); // area is now 100
```

**Note:** Use `.map()` when transforming a single signal. Use `Signal.computed()` when you need to access multiple signals - it will automatically track all signals you access within the lambda.

**Complex computed signals:**
```java
import com.vaadin.signals.shared.SharedListSignal;

SharedListSignal<Task> tasks = new SharedListSignal<>(Task.class);

Signal<Integer> completedCount = Signal.computed(() ->
    (int) tasks.value().stream()
        .map(ValueSignal::value)
        .filter(Task::completed)
        .count()
);

Signal<String> status = Signal.computed(() -> {
    int total = tasks.value().size();
    int completed = completedCount.value();
    return completed + " / " + total + " completed";
});
```

## Important Distinctions

### SharedListSignal<T> vs ValueSignal<List<T>>

**SharedListSignal<T>:**
- A shared signal containing a list of signals
- Tracks both list structure changes AND individual item changes
- When you read from a SharedListSignal, you get `List<ValueSignal<T>>`
- Use when you need reactivity for both the collection and items
- Best for shared/collaborative lists

```java
import com.vaadin.signals.shared.SharedListSignal;

SharedListSignal<Task> tasks = new SharedListSignal<>(Task.class);

// Get list of signals
List<ValueSignal<Task>> signals = tasks.value();

// Update individual item - UI updates!
signals.get(0).value(newTask);

// Add item - UI updates!
tasks.insertLast(anotherTask);
```

**ValueSignal<List<T>>:**
- A local signal holding a plain list
- Only notifies when the entire list reference changes
- Use for local UI state where you replace the whole list
- Simpler when you don't need individual item reactivity

```java
import com.vaadin.signals.local.ValueSignal;

ValueSignal<List<String>> items = new ValueSignal<>(new ArrayList<>());

// Must replace entire list to trigger updates
List<String> newList = new ArrayList<>(items.value());
newList.add("New item");
items.value(newList);
```

### Local vs Shared Signals

**Use Local Signals (`ValueSignal`) when:**
- State is specific to one view/component
- No need to share between users
- Form fields, UI toggles, local filters

**Use Shared Signals (`SharedValueSignal`, `SharedListSignal`, `SharedMapSignal`) when:**
- State needs to be shared across multiple users
- Building collaborative features (chat, presence, shared editing)
- Application-wide settings or counters

## General Guidelines

### Thread Safety and UI Updates

**Signals are always thread-safe and handle UI updates automatically.**

You NEVER need to wrap signal operations in `ui.access()`:

```java
// Signals handle threading automatically - no ui.access() needed
new Thread(() -> {
    nameSignal.value("Updated from background thread");
}).start();
```

This applies to all signal operations:
- Setting values: `signal.value(newValue)`
- Adding to lists: `sharedListSignal.insertLast(item)`
- Updating maps: `sharedMapSignal.put(key, value)`
- Computed signals: automatically update from any thread

### Sharing Signals Between Users

**Shared signals can be shared globally via static fields or application-scoped beans.**

No special configuration needed - just make the signal accessible to multiple sessions:

**Using static fields:**
```java
import com.vaadin.signals.shared.SharedListSignal;

@Service
public class ChatService {
    // Shared across all users
    private static final SharedListSignal<ChatMessage> messages =
        new SharedListSignal<>(ChatMessage.class);

    public SharedListSignal<ChatMessage> getMessages() {
        return messages;
    }

    public void sendMessage(ChatMessage message) {
        messages.insertLast(message); // All connected users see this instantly
    }
}
```

**Using application-scoped beans:**
```java
import com.vaadin.signals.shared.SharedMapSignal;

@Component
@Scope("application") // or @ApplicationScoped
public class ActiveUsersSignal extends SharedMapSignal<UserInfo> {
    public ActiveUsersSignal() {
        super(UserInfo.class);
    }
}

// Inject into any view
@Route("lobby")
public class LobbyView extends VerticalLayout {
    public LobbyView(ActiveUsersSignal activeUsers) {
        // All users share the same signal instance
        Span count = new Span();
        count.bindText(activeUsers.map(users ->
            users.size() + " users online"
        ));
    }
}
```

**Key points:**
- One signal instance → all connected users see the same data
- No manual broadcast needed - UI updates automatically
- Thread-safe by default - multiple users can modify concurrently
- Works with @Push enabled for real-time updates

## Best Practices

### 1. Choose the right signal type

Use local `ValueSignal` for UI-scoped state:

```java
import com.vaadin.signals.local.ValueSignal;

ValueSignal<String> searchFilter = new ValueSignal<>("");
```

Use shared signals for multi-user state:

```java
import com.vaadin.signals.shared.SharedValueSignal;
import com.vaadin.signals.shared.SharedListSignal;

SharedValueSignal<String> globalAnnouncement = new SharedValueSignal<>("");
SharedListSignal<Task> sharedTasks = new SharedListSignal<>(Task.class);
```

### 2. Replace objects in ValueSignal, never mutate

```java
// Replace with new instance
task.value(task.value().withTitle("New Title"));
```

### 3. Use SharedListSignal for shared collections

```java
import com.vaadin.signals.shared.SharedListSignal;

SharedListSignal<String> items = new SharedListSignal<>(String.class);
items.insertLast("Item"); // UI updates automatically for all users
```

### 4. Use Signal.computed() for multi-signal dependencies

```java
// Tracks both price and quantity signals
Signal<Integer> total = Signal.computed(() ->
    priceSignal.value() * quantitySignal.value()
);
```

Note: Using `.map()` only tracks the signal being mapped, not signals accessed inside the mapper function.

### 5. Make signals fields, not local variables

```java
// Store as field to prevent garbage collection
private final Signal<String> upperName = nameSignal.map(String::toUpperCase);

public void buildUI() {
    span.bindText(upperName);
}
```

## Common Patterns

### Form with validation (Local)

```java
import com.vaadin.signals.local.ValueSignal;

private final ValueSignal<String> emailSignal = new ValueSignal<>("");
private final Signal<Boolean> isValidEmail = emailSignal.map(email ->
    email.matches("^[A-Za-z0-9+_.-]+@(.+)$")
);

TextField emailField = new TextField("Email");
emailField.bindValue(emailSignal);

Button submitButton = new Button("Submit");
submitButton.bindEnabled(isValidEmail);
```

### Computed statistics (Shared)

```java
import com.vaadin.signals.shared.SharedListSignal;

private final SharedListSignal<Task> tasks = new SharedListSignal<>(Task.class);

private final Signal<Integer> totalTasks = tasks.map(List::size);

private final Signal<Integer> completedTasks = Signal.computed(() ->
    (int) tasks.value().stream()
        .map(ValueSignal::value)
        .filter(Task::completed)
        .count()
);

private final Signal<Integer> pendingTasks = Signal.computed(() ->
    totalTasks.value() - completedTasks.value()
);
```

### Multi-user collaboration

```java
import com.vaadin.signals.shared.SharedMapSignal;

private final SharedMapSignal<UserInfo> activeUsers =
    new SharedMapSignal<>(UserInfo.class);

private final Signal<Integer> userCount = activeUsers.map(Map::size);

private final Signal<List<String>> usernames = activeUsers.map(map ->
    map.values().stream()
        .map(ValueSignal::value)
        .map(UserInfo::username)
        .sorted()
        .toList()
);

Span userCountLabel = new Span();
userCountLabel.bindText(userCount.map(count ->
    count + " user" + (count == 1 ? "" : "s") + " online"
));
```

## Troubleshooting

### Computed signal not updating

**Problem:**
```java
Signal<String> fullName = firstName.map(first ->
    first + " " + lastName.value() // lastName not tracked!
);
```

**Solution:**
```java
Signal<String> fullName = Signal.computed(() ->
    firstName.value() + " " + lastName.value()
);
```

### Signal changes not reflecting in UI

**Problem:**
```java
ValueSignal<List<String>> items = new ValueSignal<>(new ArrayList<>());
items.value().add("Item"); // No notification!
```

**Solution:**
```java
// Option 1: Use SharedListSignal for shared state
SharedListSignal<String> items = new SharedListSignal<>(String.class);
items.insertLast("Item");

// Option 2: Replace the entire list for local state
List<String> newList = new ArrayList<>(items.value());
newList.add("Item");
items.value(newList);
```

## API Quick Reference

### ValueSignal<T> (Local)
- **Package:** `com.vaadin.signals.local`
- `T value()` - Get current value
- `void value(T newValue)` - Set new value
- `void subscribe(Runnable callback)` - React to changes
- `<R> Signal<R> map(Function<T, R> mapper)` - Transform value

### SharedValueSignal<T>
- **Package:** `com.vaadin.signals.shared`
- `T value()` - Get current value
- `void value(T newValue)` - Set new value
- `void subscribe(Runnable callback)` - React to changes
- `<R> Signal<R> map(Function<T, R> mapper)` - Transform value

### SharedListSignal<T>
- **Package:** `com.vaadin.signals.shared`
- `List<ValueSignal<T>> value()` - Get list of signals
- `void insertLast(T item)` - Add to end
- `void insertFirst(T item)` - Add to start
- `void insert(int index, T item)` - Insert at position
- `void remove(ValueSignal<T> signal)` - Remove item
- `<R> Signal<R> map(Function<List<ValueSignal<T>>, R>)` - Transform

### SharedMapSignal<V>
- **Package:** `com.vaadin.signals.shared`
- `Map<String, ValueSignal<V>> value()` - Get map of signals
- `void put(String key, V value)` - Add/update entry
- `ValueSignal<V> get(String key)` - Get signal for key
- `void remove(String key)` - Remove entry
- `<R> Signal<R> map(Function<Map<String, ValueSignal<V>>, R>)` - Transform

### Signal.computed()
- `Signal.computed(Supplier<T> computation)` - Create computed signal
- Automatically tracks all signal reads in the supplier
- Re-evaluates when any dependency changes
