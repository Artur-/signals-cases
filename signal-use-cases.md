# Vaadin Signal API Use Cases - Current Implementation

This document describes the 21 use cases currently implemented in this project.

## Introduction

Signals provide a reactive, declarative approach to building UIs where component state automatically synchronizes with application state. The framework manages the dependency graph and lifecycle automatically.

---

## Single-User Use Cases (UC 1-17)

### UC 1: Dynamic Button State

**Description**: Form with validation that reactively enables/disables a submit button based on whether all required fields are filled.

**Key Patterns**:
- Computed signal combining multiple field signals
- `bindEnabled()` for button state
- Signal-based form validation

**Route**: `/use-case-1`

---

### UC 2: Progressive Disclosure with Nested Conditions

**Description**: Multi-level form with nested conditional sections. Selecting options reveals additional fields, which in turn may reveal more fields.

**Key Patterns**:
- Nested conditional rendering
- Signal chains with `map()`
- Multiple levels of `bindVisible()`

**Route**: `/use-case-2`

---

### UC 3: Permission-Based Component Visibility

**Description**: Dashboard with components that show/hide based on user roles from Spring Security.

**Key Patterns**:
- Integration with Spring Security
- Role-based UI with signals
- Application-scoped user context signal

**Route**: `/use-case-3`

---

### UC 4: Filtered and Sorted Data Grid

**Description**: Product inventory grid with client-side filtering (search, category, in-stock) and sorting.

**Key Patterns**:
- List filtering with signals
- Multiple filter signals combined
- Grid data binding with `bindItems()`

**Route**: `/use-case-4`

---

### UC 5: Cascading Location Selector

**Description**: Three dependent dropdowns (Country → State → City) where each selection filters the next dropdown's options.

**Key Patterns**:
- Cascading dependent signals
- ComboBox items binding
- Signal-driven data filtering

**Route**: `/use-case-5`

---

### UC 6: Shopping Cart with Real-time Totals

**Description**: Shopping cart with add/remove items, quantity adjustment, and reactive totals (subtotal, tax, total).

**Key Patterns**:
- List modification signals
- Aggregate computations
- Multiple derived totals

**Route**: `/use-case-6`

---

### UC 7: Master-Detail Invoice View

**Description**: Invoice with header info and line items. Totals update reactively as line items are added/modified/removed.

**Key Patterns**:
- Master-detail data binding
- Derived calculations
- Dynamic list management

**Route**: `/use-case-7`

---

### UC 8: Multi-Step Wizard with Validation

**Description**: Three-step registration wizard with per-step validation and navigation control.

**Key Patterns**:
- Step state management
- Validation signals
- Conditional navigation

**Route**: `/use-case-8`

---

### UC 9: Form with Binder Integration

**Description**: Registration form using Vaadin Binder with signal-based validation status and reactive UI feedback.

**Key Patterns**:
- Binder + signals integration
- Validation status as signal
- Cross-field validation
- Reactive error display

**Route**: `/use-case-9`

---

### UC 10: Employee Management Grid with Dynamic Editability

**Description**: Employee grid where rows are editable based on permissions and employee status.

**Key Patterns**:
- Dynamic row editability
- Permission-based grid behavior
- Cell-level conditional editing

**Route**: `/use-case-10`

---

### UC 11: Responsive Layout with Window Size Signal

**Description**: Layout that adapts to browser window size, showing different content for mobile/tablet/desktop.

**Key Patterns**:
- Browser window resize as signal source
- Responsive breakpoints
- Conditional layout rendering

**Route**: `/use-case-11`

---

### UC 12: Dynamic View Title

**Description**: Page title that updates based on view state and syncs with browser tab title.

**Key Patterns**:
- Browser document.title binding
- Signal composition (app name + view title)
- Dynamic page metadata

**Route**: `/use-case-12`

---

### UC 13: Current User Signal

**Description**: Application-wide signal holding current user information from Spring Security, used across views and layout.

**Key Patterns**:
- Application-scoped signal (Spring @Component)
- Spring Security integration
- Shared signal across components
- Role-based reactive UI

**Route**: `/use-case-13`

---

### UC 14: Async Data Loading with States

**Description**: User list view with async data loading that properly handles loading/success/error states. Shows loading spinner while fetching, displays data on success, or shows error message with retry button.

**Key Patterns**:
- `Signal<LoadingState<T>>` with Loading/Success/Error states
- Async operations with CompletableFuture
- Loading indicators during async operations
- Error handling with retry mechanism
- Optimistic updates with rollback

**Route**: `/use-case-14`

---

### UC 15: Debounced Search

**Description**: Product search with search-as-you-type functionality. Search input is debounced by 300ms to avoid excessive server calls. Shows searching indicator and cancels in-flight requests when new input arrives.

**Key Patterns**:
- Debouncing signal updates (300ms delay)
- Search only after user stops typing
- Cancel in-flight requests on new input
- Loading state during search
- Highlight matching text in results
- Search count tracking

**Route**: `/use-case-15`

---

### UC 16: Search with URL State (Router Integration)

**Description**: Article search where filters (search query and category) are synchronized with URL query parameters. Enables deep linking and browser back button support.

**Key Patterns**:
- Query parameters as signals (two-way binding)
- Update URL when signal changes
- Load signal from URL on navigation
- Back button support (browser history)
- Shareable URLs with search state
- BeforeEnterObserver integration

**Route**: `/use-case-16`

---

### UC 17: Form Auto-Save with Persistence

**Description**: Support request form with auto-save functionality. Form automatically saves draft to localStorage every 30 seconds if modified. Draft is restored on page load and cleared after successful submission.

**Key Patterns**:
- Form dirty state tracking
- Periodic auto-save (30 second interval)
- localStorage persistence via JavaScript
- Draft status signal (IDLE/DIRTY/SAVING/SAVED/LOADED)
- Visual indication of save status
- Draft restoration on page load
- Clear draft after submit

**Route**: `/use-case-17`

---

## Multi-User Collaboration (MUC 1-4)

### MUC 1: Shared Chat/Message List

**Description**: Collaborative chat where multiple users can add messages. Message list is append-only and shared across all sessions.

**Key Patterns**:
- Application-scoped shared signal
- Append-only list operations
- Real-time updates across users
- CollaborativeSignals component

**Route**: `/muc-01`

---

### MUC 2: Collaborative Cursor Positions

**Description**: Canvas where all users' cursor positions are visible to everyone in real-time.

**Key Patterns**:
- Per-user signals in shared Map
- Multiple signal observation
- Real-time position updates
- Collaborative awareness indicators

**Route**: `/muc-02`

---

### MUC 3: Competitive Button Click Game

**Description**: Game where a button appears randomly and users race to click it. Only the fastest user gets the point.

**Key Patterns**:
- Atomic operations
- Conflict resolution
- Shared leaderboard signal
- Race condition handling

**Route**: `/muc-03`

---

### MUC 4: Collaborative Form Editing with Locking

**Description**: Shared form where users can edit different fields. Fields lock when another user is editing them.

**Key Patterns**:
- Field-level locking
- Lock status signals
- Collaborative editing indicators
- Conflict prevention

**Route**: `/muc-04`

---

## Signal API Features Used

### Core Signal Operations
- `Signal.create()` / `new ValueSignal<>()` - Creating writable signals
- `signal.value()` - Getting/setting signal values
- `signal.map()` - Transforming signal values
- Computed signals combining multiple sources

### Component Bindings
- `bindValue()` - Two-way binding for form fields
- `bindText()` / `bindElementText()` - Dynamic text content
- `bindVisible()` - Conditional visibility
- `bindEnabled()` - Dynamic enabled/disabled state
- `bindStyle()` - CSS style property binding
- `bindBrowserTitle()` - Browser tab title
- `bindChildren()` - Dynamic component children
- `bindItems()` - List binding for Grid/ComboBox
- `bindHelperText()` - Field feedback text

### Advanced Patterns
- Application-scoped signals (Spring @Component)
- Browser event integration (window resize)
- Spring Security integration
- Vaadin Binder integration
- Multi-user shared signals
- Lifecycle management (onAttach/onDetach)

---

## Architecture Patterns

### Application-Scoped Signals

Three Spring components provide application-wide signals:

1. **CurrentUserSignal** - Holds current authenticated user information
2. **CollaborativeSignals** - Shared signals for multi-user use cases
3. **UserSessionRegistry** - Tracks active users with reactive signal

### Multi-User Signal Sharing

Multi-user use cases share signals across sessions:
- Signals stored in application-scoped Spring components
- All users observe the same signal instances
- Updates propagate automatically to all connected clients
- User registration via onAttach/onDetach lifecycle hooks

---

## Implementation Notes

- All use cases compile and run with **Vaadin 25.1-SNAPSHOT**
- Uses **Spring Boot 4.0.1** with Spring Security
- **MissingAPI.java** provides temporary helper methods until official Signal API is available
- Authentication required (except login page) - 4 demo users with different roles

---

**Total Use Cases**: 21 (17 single-user + 4 multi-user)
**Last Updated**: 2026-01-13
