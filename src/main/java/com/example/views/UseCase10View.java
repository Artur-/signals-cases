package com.example.views;

import com.example.MissingAPI;


// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.annotation.security.PermitAll;

@Route(value = "use-case-10", layout = MainLayout.class)
@PageTitle("Use Case 10: Employee Management Grid with Dynamic Editability")
@Menu(order = 10, title = "UC 10: Grid Providers")
@PermitAll
public class UseCase10View extends VerticalLayout {

    public enum UserRole { VIEWER, EDITOR, ADMIN }
    public enum EmployeeStatus { ACTIVE, ON_LEAVE, TERMINATED }

    public static record Employee(String id, String name, String department, EmployeeStatus status, double salary) {}

    public UseCase10View() {
        // Create signals for permissions and edit mode
        WritableSignal<UserRole> userRoleSignal = new ValueSignal<>(UserRole.VIEWER);
        WritableSignal<Boolean> editModeSignal = new ValueSignal<>(false);
        WritableSignal<Boolean> dragDropEnabledSignal = new ValueSignal<>(false);

        // Computed permission signals
        Signal<Boolean> canEditSignal = Signal.computed(() ->
            userRoleSignal.value() == UserRole.EDITOR || userRoleSignal.value() == UserRole.ADMIN
        );

        Signal<Boolean> canDeleteSignal = Signal.computed(() ->
            userRoleSignal.value() == UserRole.ADMIN
        );

        Signal<Boolean> canReorderSignal = Signal.computed(() ->
            userRoleSignal.value() == UserRole.ADMIN && dragDropEnabledSignal.value()
        );

        // Load employees
        ListSignal<Employee> employeesSignal = new ListSignal<>(Employee.class);
        loadEmployees().forEach(employeesSignal::insertLast);

        // Controls
        ComboBox<UserRole> roleSelector = new ComboBox<>("Simulate User Role", UserRole.values());
        roleSelector.setValue(UserRole.VIEWER);
        MissingAPI.bindValue(roleSelector, userRoleSignal);

        Checkbox editModeCheckbox = new Checkbox("Edit Mode");
        MissingAPI.bindValue(editModeCheckbox, editModeSignal);
        editModeCheckbox.bindEnabled(canEditSignal);

        Checkbox dragDropCheckbox = new Checkbox("Enable Drag & Drop Reordering");
        MissingAPI.bindValue(dragDropCheckbox, dragDropEnabledSignal);
        dragDropCheckbox.bindEnabled(canEditSignal);

        HorizontalLayout controls = new HorizontalLayout(roleSelector, editModeCheckbox, dragDropCheckbox);

        // Employee grid
        Grid<Employee> grid = new Grid<>(Employee.class);
        grid.setColumns("id", "name", "department", "status", "salary");
        MissingAPI.bindItems(grid, employeesSignal.map(empSignals ->
            empSignals.stream().map(ValueSignal::value).toList()
        ));

        // Dynamic cell editability based on signals
        MissingAPI.bindEditable(grid.getColumnByKey("name"), Signal.computed(() ->
            editModeSignal.value() && canEditSignal.value()
        ));

        MissingAPI.bindEditable(grid.getColumnByKey("department"), Signal.computed(() ->
            editModeSignal.value() && canEditSignal.value()
        ));

        MissingAPI.bindEditable(grid.getColumnByKey("salary"), Signal.computed(() ->
            editModeSignal.value() && canEditSignal.value() && userRoleSignal.value() == UserRole.ADMIN
        ));

        // Dynamic row selection based on employee status
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        MissingAPI.bindRowSelectable(grid, Signal.computed(() -> (Employee employee) ->
            employee.status() == EmployeeStatus.ACTIVE
        ));

        // Drag and drop reordering
        grid.setRowsDraggable(true);
        grid.setDropMode(GridDropMode.BETWEEN);
        MissingAPI.bindDragEnabled(grid, canReorderSignal);

        grid.addDragStartListener(e -> {
            // Handle drag start
        });

        grid.addDropListener(e -> {
            // Note: getDragData() is proposed API - using getDropTargetItem for now
            Employee targetEmployee = e.getDropTargetItem().orElse(null);
            if (targetEmployee != null) {
                // In real implementation, would reorder based on dragged item
                System.out.println("Drop on: " + targetEmployee.name());
            }
        });

        // Context menu with dynamic content
        GridContextMenu<Employee> contextMenu = grid.addContextMenu();

        contextMenu.setDynamicContentHandler(employee -> {
            contextMenu.removeAll();

            if (employee == null) return false;

            // Always show "View Details"
            contextMenu.addItem("View Details", e -> {
                System.out.println("Viewing details for: " + employee.name());
            });

            // Show "Edit" only if user can edit
            if (canEditSignal.value()) {
                contextMenu.addItem("Edit", e -> {
                    System.out.println("Editing: " + employee.name());
                });
            }

            // Show "Delete" only if user can delete and employee is not active
            if (canDeleteSignal.value() && employee.status() != EmployeeStatus.ACTIVE) {
                contextMenu.addItem("Delete", e -> {
                    employeesSignal.value().stream()
                        .filter(empSignal -> empSignal.value().equals(employee))
                        .findFirst()
                        .ifPresent(employeesSignal::remove);
                });
            }

            // Show status-specific actions
            if (employee.status() == EmployeeStatus.ACTIVE && canEditSignal.value()) {
                contextMenu.addItem("Mark as On Leave", e -> {
                    employeesSignal.value().stream()
                        .filter(empSignal -> empSignal.value().equals(employee))
                        .findFirst()
                        .ifPresent(empSignal -> empSignal.value(new Employee(
                            employee.id(), employee.name(), employee.department(),
                            EmployeeStatus.ON_LEAVE, employee.salary()
                        )));
                });
            }

            return true;
        });

        // Action buttons
        Button addButton = new Button("Add Employee", e -> {
            employeesSignal.insertLast(new Employee(
                "E" + (employeesSignal.value().size() + 1),
                "New Employee",
                "Unassigned",
                EmployeeStatus.ACTIVE,
                50000.0
            ));
        });
        addButton.bindEnabled(canEditSignal);

        add(
            new H3("Employee Management"),
            controls,
            addButton,
            grid
        );
    }

    private List<Employee> loadEmployees() {
        // Stub implementation - returns mock data
        return List.of(
            new Employee("E001", "John Doe", "Engineering", EmployeeStatus.ACTIVE, 85000.0),
            new Employee("E002", "Jane Smith", "Marketing", EmployeeStatus.ACTIVE, 72000.0),
            new Employee("E003", "Bob Johnson", "Engineering", EmployeeStatus.ON_LEAVE, 90000.0),
            new Employee("E004", "Alice Williams", "Sales", EmployeeStatus.ACTIVE, 68000.0),
            new Employee("E005", "Charlie Brown", "HR", EmployeeStatus.TERMINATED, 65000.0),
            new Employee("E006", "Diana Prince", "Engineering", EmployeeStatus.ACTIVE, 95000.0)
        );
    }
}
