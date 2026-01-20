# Signal API Features Not Covered in Use Cases

**Last Updated**: 2026-01-20
**Current Implementation**: 22 use cases (16 single-user + 6 multi-user)

This document lists features **explicitly mentioned** in [vaadin/platform#8366](https://github.com/vaadin/platform/issues/8366) that are **NOT** demonstrated in the current use case collection.

## Note on Coverage Status

Some patterns mentioned in earlier analysis are now partially or fully covered:
- ✅ **Async/Loading States** - Fully covered in UC14
- ✅ **Debouncing** - Custom implementation in UC15
- ✅ **URL State** - Covered in UC16
- ⚠️ **Binder Integration** - Partially covered in UC09
- ⚠️ **ComponentToggle** - Workaround with multiple `bindVisible()` in UC11

---

## 1. bindRequired()

**Issue Quote:**
> "CheckboxGroup::bindRequired(Signal<Boolean>)"

**Not Covered:** Dynamic required field status based on signals.

**Potential Use Case:**
```java
// Field becomes required based on another field's value
Checkbox isBusinessCheckbox = new Checkbox("Business Account");
isBusinessCheckbox.bindValue(isBusinessSignal);

TextField taxIdField = new TextField("Tax ID");
taxIdField.bindRequired(isBusinessSignal);  // NOT COVERED
```

---

## 2. Component-Specific Two-Way Bindings

**Issue Quote:**
> "Details::setOpened(boolean), AppLayout::setDrawerOpened(boolean), Checkbox::setIndeterminate(boolean), MenuItemBase::setChecked(boolean), Crud::setDirty(boolean)"

**Not Covered:**
- `Details::setOpened()` - Expandable/collapsible sections
- `AppLayout::setDrawerOpened()` - Navigation drawer state
- `Checkbox::setIndeterminate()` - Three-state checkboxes
- `MenuItemBase::setChecked()` - Menu item selection state
- `Crud::setDirty()` - CRUD component dirty state

**Potential Use Case:**
```java
WritableSignal<Boolean> detailsOpenSignal = Signal.create(false);

Details advancedSettings = new Details("Advanced Settings");
advancedSettings.setOpened(detailsOpenSignal);  // NOT COVERED

// Signal updates when user expands/collapses
// UI updates when signal changes programmatically
```

---

## 3. Binder Integration

**Issue Quotes:**
> "Binder should provide a readonly signal that contains the latest BinderValidationStatus state"

> "adding a signal-aware value getter to Binding"

**Partially Covered in UC09:**
- ✅ UC09 demonstrates Binder + signals integration
- ✅ Shows form validation with signals
- ❌ Does NOT use `Binder::getValidationStatus()` as signal (API not available)
- ❌ Does NOT use `Binding::value()` for cross-field validation (API not available)

**Still Missing:**
- `Binder::getValidationStatus()` returning `Signal<BinderValidationStatus<T>>`
- `Binding::value()` - Signal-aware value getter for cross-field validation
- Using `readBean()`, `writeBean()` within reactive effects

**Potential Use Case:**
```java
Binder<Person> binder = new Binder<>(Person.class);

// Get validation status as signal
Signal<BinderValidationStatus<Person>> validationSignal =
    binder.getValidationStatus();  // NOT COVERED

Button saveButton = new Button("Save");
saveButton.bindEnabled(validationSignal.map(status -> status.isOk()));

// Cross-field validation using Binding::value()
binder.forField(confirmPasswordField)
    .withValidator(confirm ->
        confirm.equals(passwordBinding.value()),  // NOT COVERED
        "Passwords must match"
    )
    .bind(Person::getPassword, Person::setPassword);
```

---

## 4. bindElementChildren()

**Issue Quote:**
> "HasComponents interface shall provide a void bindComponentChildren method along with a similar bindElementChildren method in Element"

**Not Covered:** Element-level (lower than component-level) child binding.

**What This Means:**
- We covered `bindComponentChildren()` for components
- But `bindElementChildren()` for direct DOM element manipulation is not demonstrated

**Potential Use Case:**
```java
Element customContainer = new Element("div");

Signal<List<Element>> elementsSignal = Signal.create(List.of());
customContainer.bindElementChildren(elementsSignal);  // NOT COVERED
```

---

## 5. Reactive Provider APIs for Complex Components

**Issue Quotes:**
> "Grid::setDropFilter(SerializablePredicate<T> dropFilter)"

> "GridContextMenu::setDynamicContentHandler(...)"

> "GridPro.EditColumn::setCellEditableProvider(...)"

> "Select::setItemEnabledProvider(...)"

**Status: OUT OF SCOPE / REMOVED**

**UC10 (Employee Management Grid) was removed** because:
- Advanced Grid data provider APIs are not priority for core Signal API
- Placeholder implementations in MissingAPI were incomplete
- Basic Grid usage is adequately covered in UC04, UC07

**Not Covered (Deferred):**
- `Grid::setItemSelectableProvider()` - Dynamic row selectability
- `Grid::setDropFilter()` - Dynamic drag-and-drop filtering
- `GridContextMenu::setDynamicContentHandler()` - Dynamic context menu content
- `GridPro.EditColumn::setCellEditableProvider()` - Dynamic cell editability
- `Select::setItemEnabledProvider()` - Dynamic item enabled state

**Note**: These advanced Grid features may be considered in future iterations, but are not part of the core Signal API demonstration set.

**Example (Out of Scope):**
```java
Grid<Employee> grid = new Grid<>(Employee.class);

WritableSignal<Boolean> editModeSignal = Signal.create(false);

// Only certain cells editable when in edit mode
grid.getEditColumn("salary")
    .setCellEditableProvider(employee ->
        editModeSignal.value() &&  // NOT COVERED
        employee.canEditSalary()
    );

// Dynamic item selection based on permissions
grid.setItemSelectableProvider(employee ->  // NOT COVERED
    permissionsSignal.value().canSelect(employee)
);
```

---

## 6. ComponentToggle with Selective Rendering

**Issue Quote:**
> "ComponentToggle picks a single component out of lazy-creating component instances...toggle.addExclusive(BigComponent::new, width -> width > 1000)"

**Partially Covered in UC11:**
- ✅ UC11 demonstrates responsive layout with window size signal
- ✅ Shows mutually exclusive component visibility with multiple `bindVisible()` calls
- ❌ Does NOT use `ComponentToggle` class (API not available)
- ❌ Components are eagerly created, not lazy-loaded

**Still Missing:**
- `ComponentToggle<T>` class
- `.addExclusive()` - Mutually exclusive component rendering with lazy instantiation
- `.addFallback()` - Default component when no conditions match

**What This Means:**
We used multiple `bindVisible()` calls to show/hide components, which works but is less elegant than a dedicated `ComponentToggle` utility for mutually exclusive rendering with lazy component instantiation.

**Potential Use Case:**
```java
WritableSignal<String> viewModeSignal = Signal.create("list");

ComponentToggle<String> viewToggle = new ComponentToggle<>(viewModeSignal);  // NOT COVERED

viewToggle.addExclusive("list", ListView::new);     // NOT COVERED
viewToggle.addExclusive("grid", GridView::new);     // NOT COVERED
viewToggle.addExclusive("chart", ChartView::new);   // NOT COVERED
viewToggle.addFallback(EmptyView::new);             // NOT COVERED

add(viewToggle);
// Only one view instantiated and visible at a time
```

---

## Summary

### Coverage Status

**Currently Implemented (22 use cases):**
- ✅ Async/Loading States (UC14)
- ✅ Debouncing (UC15)
- ✅ URL State Integration (UC16)
- ⚠️ Binder Integration (UC09 - partial)
- ⚠️ ComponentToggle pattern (UC11 - workaround with `bindVisible()`)

**Removed from Scope:**
- ❌ UC03: Permission-Based UI (redundant with UC02, UC11, UC13)
- ❌ UC10: Advanced Grid Providers (out of scope for core Signal API)

### High-Priority Missing API Features:
1. **`bindRequired()`** - Common in form validation, no workaround
2. **`Binder::getValidationStatus()` as Signal** - Critical for existing Vaadin applications
3. **`Binding::value()` for cross-field validation** - Essential for complex forms
4. **`ComponentToggle`** - Cleaner alternative to multiple `bindVisible()` with lazy instantiation

### Component-Specific Features (Lower Priority):
4. **Two-way bindings** for Details, AppLayout, Checkbox, Menu, Crud
5. **`bindElementChildren()`** - Element-level child binding

### Deferred (Out of Scope):
6. **Reactive provider APIs** for Grid, Select, Menu components - Advanced features, not core Signal API priority

### Recommendation:

**When API Features Become Available:**
1. **UC23: Dynamic Required Fields** - Demonstrate `bindRequired()` API
2. **UC24: Conditional Validation** - Show `Binding::value()` and `Binder::getValidationStatus()` as signals
3. **Enhance UC11** - Refactor to use `ComponentToggle` when available

**Current State:**
The 22 implemented use cases provide **excellent coverage** of the core Signal API. The main gaps are:
- Missing API features that are not yet implemented in Vaadin
- Advanced Grid provider APIs that are intentionally deferred
- Minor component-specific bindings that are lower priority

**No immediate action needed** - The current use case collection comprehensively demonstrates all available Signal API features.
