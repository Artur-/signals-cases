# Vaadin Signal API Use Cases - Current Implementation

**Last Updated**: 2026-01-20
**Current Implementation**: 22 use cases (16 single-user + 6 multi-user)

This document describes the 22 use cases currently implemented in this project.

## Introduction

Signals provide a reactive, declarative approach to building UIs where component state automatically synchronizes with application state. The framework manages the dependency graph and lifecycle automatically.

---

## Single-User Use Cases (16 total)

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

**Description**: Analytics report generation demonstrating async operations with proper loading/success/error states. Simulates heavy processing (complex calculations, data aggregation) with realistic delays. Shows loading spinner during generation, displays dashboard-style metrics on success, or shows error message with retry button.

**Key Patterns**:
- `Signal<LoadingState<T>>` with Loading/Success/Error states
- Async operations with CompletableFuture
- Loading indicators during heavy operations
- Error handling with retry mechanism
- Dashboard-style data visualization
- Realistic example for report generation, analytics, PDF creation, or complex queries

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

### UC 17: Custom PC Builder - Complex State at Scale

**Description**: E-commerce product configurator for building a custom PC. Users select components (CPU, GPU, RAM, storage, etc.) and the system reactively calculates total price, power consumption, compatibility checks, and performance estimates. Demonstrates handling ~70 interdependent signals with multi-level computed values.

**Key Patterns**:
- **Scale**: ~70 signals (12 selections + 40+ computed + 15 compatibility + 8 performance)
- **Multi-level computed signals**: Signals that depend on other computed signals (3 levels deep)
- **Complex validation**: 15+ cross-component compatibility checks (socket matching, physical clearances, power requirements)
- **Conditional options**: Available motherboards filtered by CPU socket selection
- **Multiple aggregation types**: Price totals, power consumption, performance scores
- **Real-time updates**: All 70 signals update reactively when any component changes

**Signal Categories**:
1. **Component Selections (12)**: CPU, Motherboard, RAM, GPU, 3x Storage, PSU, Case, Cooler
2. **Price Signals (11)**: Individual component prices + total
3. **Power Signals (6)**: CPU/GPU power, total consumption, PSU sufficiency, recommended wattage, margin
4. **Compatibility (15)**: Socket matching, RAM compatibility, GPU/cooler clearances, PSU wattage, slot availability
5. **Performance (8)**: CPU/GPU scores, gaming/productivity estimates, bottleneck detection, overall rating
6. **Validation (5)**: Missing components, warning messages, can build, configuration validity
7. **UI State (4)**: Show/hide detail panels, view modes

**Demonstrates**:
- Signal framework can handle 70+ signals efficiently
- Complex business logic with signals (compatibility rules, performance calculations)
- Multi-level signal dependencies remain performant
- Conditional UI based on multiple signal combinations

**Route**: `/use-case-17`

---

### UC 18: LLM-Powered Task Management

**Description**: AI-powered task management system with real-time chat interface. Users can create, update, and manage tasks through natural language conversations with an LLM assistant powered by Spring AI. Features streaming responses, task creation/updates via tool calls, and a modern chat UI.

**Key Patterns**:
- Spring AI integration with OpenAI
- Streaming LLM responses with signal updates
- Tool calling for task management (create, update, delete tasks)
- Real-time chat message display with `bindChildren()`
- ListSignal for reactive task list
- Async operations with CompletableFuture
- Dynamic UI updates during streaming

**Route**: `/use-case-18`

---

### UC 20: User Preferences

**Description**: Session-scoped user preferences that persist across page navigations within the same session. Demonstrates session-level state management with signals, showing how preferences can be stored per-user and maintained throughout their browsing session.

**Key Patterns**:
- Session-scoped signals (per-user state)
- Preference persistence within session
- Two-way binding for preference controls
- Application of preferences across views
- Session lifecycle management

**Route**: `/use-case-20`

---

## Multi-User Collaboration (6 total)

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

### MUC 6: Shared Task List

**Description**: Collaborative task management where multiple users can view and edit a shared task list in real-time. Tasks can be added, edited inline, marked complete, or deleted. All changes are immediately visible to all connected users.

**Key Patterns**:
- ListSignal for shared task collection
- Inline editing with signal-based forms
- Real-time collaboration with Push updates
- Computed statistics (total, completed, pending)
- Task completion tracking
- Delete operations with confirmation

**Route**: `/muc-06`

---

### MUC 7: Shared LLM Task List

**Description**: Multi-user version of UC18 where multiple users can collaborate on a shared task list with AI assistance. Extends AbstractTaskChatView with collaborative signals for zero code duplication. All users see the same tasks and can interact with the LLM assistant.

**Key Patterns**:
- Extends AbstractTaskChatView for code reuse
- Application-scoped shared signals (tasks and chat)
- Multi-user LLM integration
- Real-time collaborative task management
- Shared chat interface with multiple users
- Zero code duplication between UC18 and MUC07

**Route**: `/muc-07`

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

Multi-user use cases (MUC01-04, MUC06-07) share signals across sessions:
- Signals stored in application-scoped Spring components (CollaborativeSignals)
- All users observe the same signal instances
- Updates propagate automatically to all connected clients via Vaadin Push
- User registration via onAttach/onDetach lifecycle hooks
- Demonstrates MapSignal (cursors, locks, scores) and ListSignal (chat, tasks) in shared context

---

## Implementation Notes

- All use cases compile and run with **Vaadin 25.1-SNAPSHOT**
- Uses **Spring Boot 4.0.1** with Spring Security
- **MissingAPI.java** provides temporary helper methods until official Signal API is available
- Authentication required (except login page) - 4 demo users with different roles

---

## Recent Changes

**2026-01-20 Update:**
- Removed UC3 (Permission-Based UI) - redundant with UC2/UC11/UC13
- Removed UC10 (Grid Providers) - advanced Grid APIs out of scope
- Updated documentation to reflect 22 use cases (16 single-user + 6 multi-user)

---

**Total Use Cases**: 22 (16 single-user + 6 multi-user)
**Last Updated**: 2026-01-20
