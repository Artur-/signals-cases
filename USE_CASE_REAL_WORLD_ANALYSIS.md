# Real-World Use Case Analysis

**Last Updated**: 2026-01-20
**Current Implementation**: 22 use cases (16 single-user + 6 multi-user)

## Executive Summary

This document analyzes the current use case collection to determine if it's driven by **real-world web application requirements** or merely by **what the proposed Signal API happens to support**. The goal is to identify missing patterns that real applications need, which may require API extensions.

## Current Coverage (22 Implemented Use Cases)

### ✅ Well Covered Patterns

**Basic Reactive UI (UC01, UC02, UC11, UC13)**
- Component visibility based on state - ✅ UC02, UC11
- Enable/disable based on conditions - ✅ UC01, UC08
- Text binding to signals - ✅ UC01, UC06, UC12
- Permission-based UI rendering - ✅ UC13 (Spring Security)
- Responsive layouts - ✅ UC11 (window size signal)
- **Real-world need**: YES - fundamental patterns in any app

**Computed/Derived Values (UC05, UC06, UC07, UC17)**
- Shopping cart totals - ✅ UC06
- Invoice line item calculations - ✅ UC07
- Cascading selectors - ✅ UC05
- Master-detail views - ✅ UC07
- Complex interdependent state - ✅ UC17 (~70 signals)
- **Real-world need**: YES - essential for business logic

**Form Handling (UC01, UC02, UC08, UC09)**
- Multi-step wizards - ✅ UC08
- Binder integration with validation - ✅ UC09 (partial)
- Progressive disclosure - ✅ UC02
- Dynamic form validation - ✅ UC01, UC08
- **Real-world need**: PARTIAL - **missing dirty state tracking, conditional validation**

**List/Grid Rendering (UC04, UC06, UC07)**
- Filtered/sorted data - ✅ UC04
- Dynamic task lists - ✅ UC06
- Master-detail grids - ✅ UC07
- **Real-world need**: PARTIAL - **missing pagination, multi-selection with bulk actions**

**Multi-User Collaboration (MUC01-04, MUC06-07)**
- Shared chat - ✅ MUC01
- Cursor positions - ✅ MUC02
- Field locking - ✅ MUC04
- Conflict resolution - ✅ MUC03
- Collaborative task management - ✅ MUC06, MUC07
- **Real-world need**: YES - cutting-edge, differentiator feature

**Browser Integration (UC11, UC12, UC13, UC20)**
- Responsive layout (window size) - ✅ UC11
- Dynamic browser title - ✅ UC12
- Current user signal - ✅ UC13
- User preferences - ✅ UC20
- **Real-world need**: YES - practical utility patterns

**Visual & Graphics (UC03)**
- SVG manipulation with attribute binding - ✅ UC03
- Real-time visual property updates - ✅ UC03
- Complex computed transformations - ✅ UC03
- **Real-world need**: MEDIUM - dashboards, data visualization, graphic apps

**Advanced Patterns (UC14, UC15, UC16, UC18)**
- Async operations & loading states - ✅ UC14
- Debounced search - ✅ UC15
- URL state integration - ✅ UC16
- LLM integration - ✅ UC18
- **Real-world need**: YES - modern web app features

## ✅ Recently Addressed Patterns

### 1. Async Operations & Loading States ✅ COVERED
**Real-world need: CRITICAL**

**UC14: Async Data Loading** implements:
- `Signal<LoadingState<T>>` pattern
- Loading spinner display
- Success data rendering
- Error message with retry
- State transitions: Loading → Success/Error

**Status**: ✅ **FULLY COVERED** - Pattern is demonstrated

### 2. Debouncing & Throttling ✅ COVERED
**Real-world need: CRITICAL**

**UC15: Debounced Search** implements:
- Search-as-you-type with debouncing
- 300ms debounce delay
- Custom debouncing implementation
- Search results display

**Status**: ✅ **FULLY COVERED** - Workaround exists, official API would be better

### 3. Route/Query Parameters as Signals ✅ COVERED
**Real-world need: HIGH**

**UC16: URL State Integration** implements:
- Query parameters as signals
- Two-way sync: URL ↔ Signal
- Router integration pattern
- Deep linking support

**Status**: ✅ **FULLY COVERED** - Router integration pattern demonstrated

## 🚨 Missing Critical Patterns

### 1. Pagination & Infinite Scroll ❌ MISSING
**Real-world need: CRITICAL**

Large datasets require pagination:
- Current page signal
- Page size signal
- Total count signal
- Next/previous navigation
- Jump to page
- Server-side data loading

**Missing Use Case:**
```
UC19: Paginated Data Grid
- Grid with server-side data loading
- Signal<Integer> currentPage
- Signal<Integer> pageSize
- Computed offset signal
- Pagination controls (page 1 of 10)
- Loading indicator during page change
```

**Status**: ❌ **NOT COVERED** - Essential pattern missing

### 2. Form Dirty State & Unsaved Changes ❌ MISSING
**Real-world need: CRITICAL**

Forms need to track modifications:
- Detect if form has unsaved changes
- Warn user before navigation
- Reset to original values
- Compare current vs. original state

**Missing Use Case:**
```
UC21: Form with Dirty State Tracking
- Signal<Boolean> formDirty computed from field changes
- "You have unsaved changes" warning on navigation
- "Reset" button to restore original values
- "Save" button enabled only when dirty
- Visual indicator of modified fields
```

**Status**: ❌ **NOT COVERED** - Critical form pattern missing

### 3. Selection State Management ❌ MISSING
**Real-world need: CRITICAL**

Grids and lists need selection:
- Multiple selection with checkboxes
- Select all / deselect all
- Bulk operations on selected items
- Selection count display

**Missing Use Case:**
```
UC22: Grid with Multi-Select and Bulk Actions
- Grid with checkbox column
- Signal<Set<T>> selectedItems
- "Select All" / "Deselect All" buttons
- Bulk delete button (enabled when selection not empty)
- Selection count: "3 items selected"
```

**Status**: ❌ **NOT COVERED** - Common CRUD pattern missing

### 4. Toast/Notification Queue ❌ MISSING
**Real-world need: HIGH**

Apps need global notifications:
- Success/error/warning/info messages
- Auto-dismiss after timeout
- Queue multiple messages
- Manual dismiss
- Position configuration

**Missing Use Case:**
```
UC25: Global Notification System
- Signal<List<Notification>> notificationQueue
- Add notification from anywhere
- Auto-dismiss after 5 seconds
- Click to dismiss manually
- Multiple notifications stack vertically
```

**Status**: ❌ **NOT COVERED** - Common UX pattern

### 5. Conditional Validation Rules ❌ MISSING
**Real-world need: MEDIUM**

Validation depends on other fields:
- "End date required if start date is set"
- "Phone OR email required (at least one)"
- "Credit card fields required if payment method = 'card'"

**Missing Use Case:**
```
UC24: Form with Conditional Validation
- Payment method selection (cash/card)
- Credit card fields shown only if method = 'card'
- Validation rules change based on payment method
- Cross-field validation with Binding.value()
- Error messages update reactively
```

**Status**: ❌ **NOT COVERED** - Needs `Binding.value()` API feature

### 6. Auto-Save Drafts ❌ MISSING
**Real-world need: MEDIUM-HIGH**

Long forms need auto-save:
- Periodically save to server
- Save after debounced inactivity
- Show "Draft saved at 14:32" indicator
- Restore draft on page load
- Clear draft after submit

**Missing Use Case:**
```
UC26: Form with Auto-Save
- Save draft every 30 seconds if form is dirty
- Signal<DraftStatus> showing last save time
- Load draft on view initialization
- Clear draft after successful submit
```

**Status**: ❌ **NOT COVERED** - Would benefit from dirty state pattern + timer

### 7. Undo/Redo ❌ MISSING
**Real-world need: MEDIUM**

Rich editors and complex forms:
- Undo last action
- Redo undone action
- Undo/Redo button enabled state
- Keyboard shortcuts (Ctrl+Z, Ctrl+Y)

**Missing Use Case:**
```
UC27: Text Editor with Undo/Redo
- Text area with content signal
- History stack of previous values
- Undo button (enabled when history not empty)
- Redo button (enabled when forward history exists)
```

**Status**: ❌ **NOT COVERED** - Advanced pattern, lower priority

### 8. Theme/Preferences Toggle ❌ PARTIAL
**Real-world need: MEDIUM**

User preferences:
- Dark mode toggle
- Language selection
- Persist to localStorage
- Apply across all views

**Partial Coverage**: UC20 demonstrates user preferences with session-scoped signals, but doesn't implement theme switching or persistence.

**Missing Use Case:**
```
UC28: Dark Mode Toggle with Persistence
- Toggle switch for dark mode
- Signal<Boolean> darkMode
- Apply theme to all components
- Save preference to localStorage
- Load preference on app start
```

**Status**: ⚠️ **PARTIALLY COVERED** - UC20 shows preferences pattern, missing theme application

## Removed/Replaced Use Cases

### UC03: Permission-Based UI → Interactive SVG Shape Editor ✅ REPLACED
- **Old UC03** (Permission-Based UI) removed due to UX issues without dynamic user switching
- **Redundancy**: `bindVisible()` covered in UC02, UC11; Spring Security in UC13
- **New UC03** (Interactive SVG Shape Editor) demonstrates:
  - Extensive `bindAttribute()` usage with SVG elements
  - ~28 signals controlling shapes (circle, rectangle, star)
  - Computed SVG attributes (transform, points)
  - Real-time visual feedback
  - Fills gap in attribute binding demonstration
- **Real-world need**: MEDIUM - Visual/graphic applications, dashboards, data visualization

### UC10: Employee Management Grid ❌ REMOVED
- **Reason**: Advanced Grid data provider APIs out of scope
- **Coverage**: Basic Grid usage in UC04, UC07
- **API Gap**: `bindEditable()`, `bindRowSelectable()`, `bindDragEnabled()` are placeholders

## Summary: Coverage vs. Real-World Needs

### Priority 0: Critical & Missing
1. ❌ **Pagination** (UC19) - Essential for large datasets
2. ❌ **Form Dirty State** (UC21) - Critical for forms
3. ❌ **Multi-Selection + Bulk Actions** (UC22) - Common CRUD pattern

### Priority 1: Common & Missing
4. ❌ **Toast/Notification Queue** (UC25) - Common UX pattern
5. ❌ **Conditional Validation** (UC24) - Needs API support
6. ❌ **Auto-Save Drafts** (UC26) - Medium-high value

### Priority 2: Advanced & Missing
7. ❌ **Undo/Redo** (UC27) - Advanced pattern
8. ⚠️ **Theme Toggle** (UC28) - Partial in UC20

### Already Covered ✅
- ✅ **Async/Loading States** (UC14)
- ✅ **Debounced Search** (UC15)
- ✅ **URL State** (UC16)
- ✅ **Responsive Layout** (UC11)
- ✅ **Current User Signal** (UC13)
- ✅ **Dynamic View Title** (UC12)
- ✅ **Multi-User Collaboration** (MUC01-07)

## API Extensions Needed

Based on the analysis, the Signal API would benefit from these extensions:

### 1. Official Debouncing API
```java
// Current: Custom implementation in UC15
// Ideal: Built-in debouncing
Signal<String> debouncedQuery = searchQuery.debounce(Duration.ofMillis(300));
```

### 2. Binding Validation Integration
```java
// For UC24: Conditional Validation
Binding.value() // Access binding values as signals for cross-field validation
Binder.getValidationStatus() // Validation status as signal
```

### 3. Dynamic Required Fields
```java
// For UC23: Dynamic Required Fields
bindRequired(Signal<Boolean>) // Make field required based on signal
```

### 4. Signal History (Lower Priority)
```java
// For UC27: Undo/Redo
SignalHistory<String> history = signal.withHistory();
history.undo();
history.redo();
Signal<Boolean> canUndo = history.canUndo();
```

### 5. Signal Persistence (Lower Priority)
```java
// For UC28: Theme Toggle
WritableSignal<Boolean> darkMode = Signal.persisted("darkMode", false);
```

## Recommended Next Steps

### Immediate (Phase 1)
1. **Implement UC19** (Pagination) - Critical missing pattern
2. **Implement UC21** (Dirty State) - Critical for forms
3. **Implement UC22** (Multi-Selection) - Common CRUD pattern

### Short-term (Phase 2)
4. **Implement UC24** (Conditional Validation) - When API available
5. **Implement UC23** (Dynamic Required) - When API available
6. **Implement UC25** (Notifications) - Common UX pattern

### Long-term (Phase 3)
7. Consider UC26 (Auto-Save), UC27 (Undo/Redo), UC28 (Theme Toggle)

## Conclusion

**Current assessment:**
- ✅ **Excellent coverage** of basic reactive patterns
- ✅ **Excellent coverage** of multi-user collaboration (6 MUC cases)
- ✅ **Good coverage** of async operations (UC14), debouncing (UC15), routing (UC16)
- ❌ **Missing** pagination, form dirty state, multi-selection
- ⚠️ **Partial** conditional validation (needs API), theme toggle (partial in UC20)

**Answer to original question:** The current use cases are now **well-balanced**:
- Comprehensive core Signal API coverage
- Strong multi-user collaboration examples
- Most critical real-world patterns are covered
- **Three key patterns missing**: pagination, dirty state, multi-selection

**Recommendation**: Add UC19 (Pagination), UC21 (Dirty State), and UC22 (Multi-Selection) to complete the essential pattern library. This would bring the total to **25 use cases** with comprehensive real-world coverage.
