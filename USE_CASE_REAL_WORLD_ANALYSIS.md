# Real-World Use Case Analysis

## Executive Summary

This document analyzes the current use case collection to determine if it's driven by **real-world web application requirements** or merely by **what the proposed Signal API happens to support**. The goal is to identify missing patterns that real applications need, which may require API extensions.

## Current Coverage (UC 1-24)

### ✅ Well Covered Patterns

**Basic Reactive UI (UC 1-3, 6-8)**
- Component visibility based on state
- Enable/disable based on conditions
- Text binding to signals
- Permission-based UI rendering
- ✓ **Real-world need**: YES - fundamental patterns in any app

**Computed/Derived Values (UC 4-5, 11-13)**
- Shopping cart totals
- Pricing calculators
- Cascading selectors
- Master-detail views
- ✓ **Real-world need**: YES - essential for business logic

**Form Handling (UC 2, 3, 14-15)**
- Multi-step wizards
- Binder integration with validation
- Field synchronization
- ✓ **Real-world need**: PARTIAL - missing key patterns (see below)

**List/Grid Rendering (UC 9-10, 17)**
- Filtered/sorted data
- Dynamic task lists
- Grid with dynamic editability
- ✓ **Real-world need**: PARTIAL - missing pagination, selection, async loading

**Multi-User Collaboration (UC 18-21)**
- Shared chat
- Cursor positions
- Field locking
- Conflict resolution
- ✓ **Real-world need**: YES - cutting-edge, differentiator feature

**Browser Integration (UC 22-24)**
- Responsive layout (window size)
- Dynamic browser title
- Current user signal
- ✓ **Real-world need**: YES - practical utility patterns

## 🚨 Missing Critical Patterns

### 1. Async Operations & Loading States
**Real-world need: CRITICAL**

Every web app needs to:
- Show loading spinners while fetching data
- Display error messages when operations fail
- Implement retry mechanisms
- Handle optimistic updates with rollback

**Missing Use Case:**
```
UC 25: Data Loading with States
- Signal<LoadingState<T>> where LoadingState = Loading | Success(T) | Error(msg)
- Show spinner while loading
- Display data on success
- Show error message with retry button
- Optimistic update: update UI immediately, rollback on error
```

**API Gap:** Need pattern for async signal updates, error handling, and loading state representation.

### 2. Debouncing & Throttling
**Real-world need: CRITICAL**

Search-as-you-type is ubiquitous:
- Don't fire search on every keystroke
- Debounce input to reduce server load
- Show "searching..." indicator
- Cancel previous requests

**Missing Use Case:**
```
UC 26: Debounced Search
- TextField with search query
- Debounce signal updates (300ms)
- Cancel in-flight requests on new input
- Show search results with loading state
- Highlight matching text
```

**API Gap:** Need `signal.debounce(duration)` or similar API.

### 3. Pagination & Infinite Scroll
**Real-world need: CRITICAL**

Large datasets require pagination:
- Current page signal
- Page size signal
- Total count signal
- Next/previous navigation
- Jump to page
- Infinite scroll variant

**Missing Use Case:**
```
UC 27: Paginated Data Grid
- Grid with server-side data loading
- Pagination controls (page 1 of 10)
- Page size selector
- Loading indicator during page change
- Preserve filter/sort state across pages
```

**API Gap:** Need patterns for server-side data loading with signals.

### 4. Form Dirty State & Unsaved Changes
**Real-world need: CRITICAL**

Forms need to track modifications:
- Detect if form has unsaved changes
- Warn user before navigation
- Reset to original values
- Compare current vs. original state

**Missing Use Case:**
```
UC 28: Form with Dirty State Tracking
- Signal<Boolean> formDirty computed from field changes
- "You have unsaved changes" warning on navigation
- "Reset" button to restore original values
- "Save" button enabled only when dirty
- Visual indicator of modified fields
```

**API Gap:** Need pattern for comparing signal value with original value.

### 5. Selection State Management
**Real-world need: CRITICAL**

Grids and lists need selection:
- Single selection
- Multiple selection with checkboxes
- Select all / deselect all
- Bulk operations on selected items
- Selection count display

**Missing Use Case:**
```
UC 29: Grid with Multi-Select and Bulk Actions
- Grid with checkbox column
- Signal<Set<T>> selectedItems
- "Select All" / "Deselect All" buttons
- Bulk delete button (enabled when selection not empty)
- Selection count: "3 items selected"
```

**API Gap:** May work with existing API, but pattern needs documentation.

### 6. Toast/Notification Queue
**Real-world need: HIGH**

Apps need global notifications:
- Success/error/warning/info messages
- Auto-dismiss after timeout
- Queue multiple messages
- Manual dismiss
- Position (top-right, bottom-left, etc.)

**Missing Use Case:**
```
UC 30: Global Notification System
- Signal<List<Notification>> notificationQueue
- Add notification from anywhere
- Auto-dismiss after 5 seconds
- Click to dismiss manually
- Multiple notifications stack vertically
```

**API Gap:** Pattern for managing a queue with auto-removal.

### 7. Route/Query Parameters as Signals
**Real-world need: HIGH**

Deep linking requires routing integration:
- Route parameters as signals
- Query parameters as signals
- Update URL when signal changes
- Two-way sync: URL ↔ Signal

**Missing Use Case:**
```
UC 31: Search with URL State
- Search query from query parameter: ?q=vaadin
- Filter from query parameter: ?category=flow
- Update URL when user types/selects
- Back button restores previous search
- Shareable URLs with search state
```

**API Gap:** Need integration between signals and Vaadin Router.

### 8. Auto-Save Drafts
**Real-world need: MEDIUM-HIGH**

Long forms need auto-save:
- Periodically save to server
- Save after debounced inactivity
- Show "Draft saved at 14:32" indicator
- Restore draft on page load
- Clear draft after submit

**Missing Use Case:**
```
UC 32: Form with Auto-Save
- Save draft every 30 seconds if form is dirty
- Signal<DraftStatus> showing last save time
- Load draft on view initialization
- Clear draft after successful submit
```

**API Gap:** Pattern for periodic side effects triggered by signal changes.

### 9. Real-time Server Updates (SSE/WebSocket)
**Real-world need: MEDIUM-HIGH**

Real-time data from server:
- Server-sent events as signal source
- WebSocket messages as signals
- Polling with signals
- Connection status indicator

**Missing Use Case:**
```
UC 33: Live Dashboard with Server-Sent Events
- Connect to SSE endpoint
- Signal<DashboardData> updated from server events
- Connection status indicator
- Auto-reconnect on disconnect
- Display real-time metrics
```

**API Gap:** Pattern for external event sources updating signals.

### 10. Conditional Validation Rules
**Real-world need: MEDIUM**

Validation depends on other fields:
- "End date required if start date is set"
- "Phone OR email required (at least one)"
- "Credit card fields required if payment method = 'card'"

**Missing Use Case:**
```
UC 34: Form with Conditional Validation
- Payment method selection (cash/card)
- Credit card fields shown only if method = 'card'
- Validation rules change based on payment method
- Error messages update reactively
```

**API Gap:** May work with existing Binder + signals, needs documentation.

### 11. Undo/Redo
**Real-world need: MEDIUM**

Rich editors and complex forms:
- Undo last action
- Redo undone action
- Undo/Redo button enabled state
- Keyboard shortcuts (Ctrl+Z, Ctrl+Y)

**Missing Use Case:**
```
UC 35: Text Editor with Undo/Redo
- Text area with content signal
- History stack of previous values
- Undo button (enabled when history not empty)
- Redo button (enabled when forward history exists)
```

**API Gap:** Pattern for maintaining signal history.

### 12. Theme/Preferences Toggle
**Real-world need: MEDIUM**

User preferences:
- Dark mode toggle
- Language selection
- Persist to localStorage
- Apply across all views

**Missing Use Case:**
```
UC 36: Dark Mode Toggle with Persistence
- Toggle switch for dark mode
- Signal<Boolean> darkMode
- Apply theme to all components
- Save preference to localStorage
- Load preference on app start
```

**API Gap:** Pattern for signal persistence.

## Summary: API Extensions Needed

Based on the analysis, the Signal API needs these extensions to handle real-world requirements:

### 1. Async & Loading State
```java
// Pattern for loading states
Signal<LoadingState<T>> dataSignal = Signal.async(() -> fetchFromServer());

enum LoadingState<T> {
    Loading,
    Success(T data),
    Error(String message)
}
```

### 2. Debouncing & Throttling
```java
// Debounce signal updates
Signal<String> debouncedQuery = searchQuery.debounce(Duration.ofMillis(300));

// Throttle signal updates
Signal<Integer> throttledScroll = scrollPosition.throttle(Duration.ofMillis(100));
```

### 3. Signal Effects with Cleanup
```java
// Side effects that run when signal changes
signal.effect(() -> {
    // Setup
    var connection = openConnection();

    // Cleanup function
    return () -> connection.close();
});
```

### 4. Signal History
```java
// For undo/redo
SignalHistory<String> history = signal.withHistory();
history.undo();
history.redo();
Signal<Boolean> canUndo = history.canUndo();
```

### 5. Signal Persistence
```java
// Persist signal to localStorage
WritableSignal<Boolean> darkMode = Signal.persisted("darkMode", false);
```

### 6. Batching Updates
```java
// Update multiple signals atomically
Signal.batch(() -> {
    signal1.value(newValue1);
    signal2.value(newValue2);
    // UI updates only once after batch
});
```

### 7. Router Integration
```java
// Route parameter as signal
Signal<String> productId = router.param("productId");

// Query parameter as signal
WritableSignal<String> searchQuery = router.queryParam("q");
```

## Recommended Next Steps

1. **Implement missing critical patterns** (UC 25-29)
2. **Document API extensions needed** for async, debouncing, persistence
3. **Validate API design** with real-world scenarios before finalization
4. **Prioritize** based on frequency of use in actual applications:
   - P0: Async/loading states, debouncing, pagination
   - P1: Form dirty state, selection management, notifications
   - P2: Router integration, auto-save, undo/redo

## Conclusion

**Current assessment:**
- ✅ Good coverage of basic reactive patterns
- ✅ Excellent collaborative multi-user patterns
- ⚠️ Missing critical async/loading patterns
- ⚠️ Missing debouncing for performance
- ⚠️ Missing pagination for large datasets
- ⚠️ Missing form dirty state tracking
- ⚠️ Missing selection management

**Answer to original question:** The current use cases are a **mix of both**:
- Some are API-driven (showing off what signals can do)
- Some are business-driven (shopping cart, forms, grids)
- But several **critical real-world patterns are missing**

The API needs extensions to handle: async operations, debouncing, persistence, and router integration. Without these, developers will resort to workarounds or manual state management, defeating the purpose of the Signal API.
