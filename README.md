# Vaadin Signal API Use Cases

This repository contains a collection of Vaadin views demonstrating the Signal API for reactive state management.

## Overview

The Vaadin Signal API provides reactive state management for Vaadin Flow applications. This project contains **22 implemented use cases** showcasing various signal patterns and real-world UI scenarios.

## Use Cases

### Single-User Use Cases (16 total)

1. **Dynamic Button State** - Form validation with reactive button enable/disable
2. **Progressive Disclosure** - Nested conditional form sections
4. **Filtered Data Grid** - Client-side filtering and sorting
5. **Cascading Selector** - Dependent dropdown menus (country → state → city)
6. **Shopping Cart** - Real-time cart totals and item management
7. **Master-Detail Invoice** - Invoice header with reactive line item calculations
8. **Multi-Step Wizard** - Form wizard with validation and navigation
9. **Form with Binder Integration** - Vaadin Binder with signal-based validation
11. **Responsive Layout** - Window size as signal for responsive UI
12. **Dynamic View Title** - Reactive page title sync with browser tab
13. **Current User Signal** - Application-wide user context from Spring Security
14. **Async Data Loading** - Loading/success/error states for async operations
15. **Debounced Search** - Search-as-you-type with 300ms debouncing
16. **URL State Integration** - Query parameters as signals with router integration
17. **Custom PC Builder** - Complex state at scale with ~70 interdependent signals
18. **LLM-Powered Task Management** - AI assistant with real-time chat interface
20. **User Preferences** - Session-scoped signal for user settings

### Multi-User Collaboration (6 total)

1. **Shared Chat** - Collaborative message list with append-only operations
2. **Collaborative Cursors** - Real-time cursor position sharing
3. **Click Race Game** - Conflict resolution and atomic operations
4. **Collaborative Editing** - Field-level locking for concurrent form editing
6. **Shared Task List** - Real-time collaborative task management with inline editing
7. **Shared LLM Task List** - Multi-user LLM-powered task management with chat

## Project Structure

```
src/main/java/com/example/
├── security/
│   ├── CurrentUserSignal.java      # Application-scoped user context signal
│   ├── SecurityConfiguration.java   # Spring Security setup
│   └── SecurityService.java
├── signals/
│   ├── CollaborativeSignals.java   # Shared signals for MUC use cases
│   └── UserSessionRegistry.java    # Active user tracking
├── views/
│   ├── MainLayout.java             # App layout with navigation
│   ├── LoginView.java              # Login page
│   ├── HomeView.java               # Landing page
│   ├── UseCase01View.java          # UC 1-13 (single-user)
│   ├── ...
│   ├── UseCase13View.java
│   ├── MUC01View.java              # MUC 1-4 (multi-user)
│   ├── ...
│   └── MUC04View.java
└── MissingAPI.java                 # Helper methods for signal bindings
```

## Running the Application

1. **Prerequisites**: Java 21+, Maven 3.9+
2. **Build**: `mvn clean install`
3. **Run**: `mvn spring-boot:run`
4. **Access**: Open http://localhost:8080

### Login Credentials

- `viewer` / `password` (VIEWER role)
- `editor` / `password` (EDITOR role)
- `admin` / `password` (ADMIN role)
- `superadmin` / `password` (SUPER_ADMIN role)

## Key Patterns Demonstrated

### Basic Reactivity
- One-way and two-way bindings
- Computed/derived signals
- Signal transformations with `map()`

### UI Patterns
- Conditional rendering (`bindVisible`, `bindEnabled`)
- Dynamic text content (`bindText`, `bindElementText`)
- Responsive layouts (browser window size as signal)
- Form validation with signals

### Advanced Patterns
- Vaadin Binder integration with signals
- Application-scoped signals (Spring @Component)
- Multi-user collaboration with shared signals
- Browser integration (window size, page title)
- Spring Security integration

## MissingAPI Helpers

Since the official Signal API is still in development, `MissingAPI.java` provides temporary helper methods:

- `bindValue()` - Two-way binding for form fields
- `bindVisible()` - Conditional visibility
- `bindEnabled()` - Dynamic enabled/disabled state
- `bindText()` - Dynamic text content
- `bindBrowserTitle()` - Browser tab title binding
- `bindChildren()` - Dynamic component children
- `bindItems()` - List binding for Grid/ComboBox

## Multi-User Architecture

Multi-user use cases (MUC 01-04, 06-07) demonstrate collaborative features using:

- **CollaborativeSignals** - Application-scoped Spring component holding shared signals
- **UserSessionRegistry** - Tracks active users with reactive signal
- **onAttach/onDetach** - Lifecycle hooks for user registration

All users see real-time updates via Vaadin's automatic UI synchronization.

## Documentation

- **signal-use-cases.md** - Detailed descriptions and patterns (TO BE UPDATED)
- **REORGANIZATION_PLAN.md** - Project structure decisions
- **USE_CASE_REAL_WORLD_ANALYSIS.md** - Analysis of real-world requirements

## Technical Stack

- **Vaadin 25.1-SNAPSHOT** - Web framework with Signal API
- **Spring Boot 4.0.1** - Application framework
- **Spring Security** - Authentication and authorization
- **Java 21** - Language and platform
- **Maven** - Build tool

## Related Resources

- [Vaadin Signal API Proposal](https://github.com/vaadin/platform/issues/8366)
- [Vaadin Flow Documentation](https://vaadin.com/docs/latest/flow)
- [Spring Security with Vaadin](https://vaadin.com/docs/latest/flow/security)

---

**Status**: Active development
**Last Updated**: 2026-01-20
**Total Use Cases**: 22 (16 single-user + 6 multi-user)
