package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
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

@Route(value = "use-case-17", layout = MainLayout.class)
@PageTitle("Use Case 17: Employee Management Grid with Dynamic Editability")
@Menu(order = 63, title = "UC 17: Grid Providers")
public class UseCase17View extends VerticalLayout {

    enum UserRole { VIEWER, EDITOR, ADMIN }
    enum EmployeeStatus { ACTIVE, ON_LEAVE, TERMINATED }

    record Employee(String id, String name, String department, EmployeeStatus status, double salary) {}

    public UseCase17View() {
        // Create signals for permissions and edit mode
        WritableSignal<UserRole> userRoleSignal = Signal.create(UserRole.VIEWER);
        WritableSignal<Boolean> editModeSignal = Signal.create(false);
        WritableSignal<Boolean> dragDropEnabledSignal = Signal.create(false);

        // Computed permission signals
        ReadableSignal<Boolean> canEditSignal = Signal.compute(() ->
            userRoleSignal.get() == UserRole.EDITOR || userRoleSignal.get() == UserRole.ADMIN
        );

        ReadableSignal<Boolean> canDeleteSignal = Signal.compute(() ->
            userRoleSignal.get() == UserRole.ADMIN
        );

        ReadableSignal<Boolean> canReorderSignal = Signal.compute(() ->
            userRoleSignal.get() == UserRole.ADMIN && dragDropEnabledSignal.get()
        );

        // Load employees
        WritableSignal<List<Employee>> employeesSignal = Signal.create(new ArrayList<>(loadEmployees()));

        // Controls
        ComboBox<UserRole> roleSelector = new ComboBox<>("Simulate User Role", UserRole.values());
        roleSelector.setValue(UserRole.VIEWER);
        roleSelector.bindValue(userRoleSignal);

        Checkbox editModeCheckbox = new Checkbox("Edit Mode");
        editModeCheckbox.bindValue(editModeSignal);
        editModeCheckbox.bindEnabled(canEditSignal);

        Checkbox dragDropCheckbox = new Checkbox("Enable Drag & Drop Reordering");
        dragDropCheckbox.bindValue(dragDropEnabledSignal);
        dragDropCheckbox.bindEnabled(canEditSignal);

        HorizontalLayout controls = new HorizontalLayout(roleSelector, editModeCheckbox, dragDropCheckbox);

        // Employee grid
        Grid<Employee> grid = new Grid<>(Employee.class);
        grid.setColumns("id", "name", "department", "status", "salary");
        grid.setItems(employeesSignal.get());

        // Dynamic cell editability based on signals
        grid.getColumnByKey("name").bindEditable(Signal.compute(() ->
            editModeSignal.get() && canEditSignal.get()
        ));

        grid.getColumnByKey("department").bindEditable(Signal.compute(() ->
            editModeSignal.get() && canEditSignal.get()
        ));

        grid.getColumnByKey("salary").bindEditable(Signal.compute(() ->
            editModeSignal.get() && canEditSignal.get() && userRoleSignal.get() == UserRole.ADMIN
        ));

        // Dynamic row selection based on employee status
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.bindRowSelectable(Signal.compute(() -> (Employee employee) ->
            employee.status() == EmployeeStatus.ACTIVE
        ));

        // Drag and drop reordering
        grid.setRowsDraggable(true);
        grid.setDropMode(GridDropMode.BETWEEN);
        grid.bindDragEnabled(canReorderSignal);

        grid.addDragStartListener(e -> {
            // Handle drag start
        });

        grid.addDropListener(e -> {
            Employee draggedEmployee = e.getDragData().orElse(null);
            Employee targetEmployee = e.getDropTargetItem().orElse(null);
            if (draggedEmployee != null && targetEmployee != null) {
                List<Employee> employees = new ArrayList<>(employeesSignal.get());
                employees.remove(draggedEmployee);
                int targetIndex = employees.indexOf(targetEmployee);
                employees.add(targetIndex, draggedEmployee);
                employeesSignal.set(employees);
                grid.setItems(employees);
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
            if (canEditSignal.get()) {
                contextMenu.addItem("Edit", e -> {
                    System.out.println("Editing: " + employee.name());
                });
            }

            // Show "Delete" only if user can delete and employee is not active
            if (canDeleteSignal.get() && employee.status() != EmployeeStatus.ACTIVE) {
                contextMenu.addItem("Delete", e -> {
                    List<Employee> employees = new ArrayList<>(employeesSignal.get());
                    employees.remove(employee);
                    employeesSignal.set(employees);
                    grid.setItems(employees);
                });
            }

            // Show status-specific actions
            if (employee.status() == EmployeeStatus.ACTIVE && canEditSignal.get()) {
                contextMenu.addItem("Mark as On Leave", e -> {
                    List<Employee> employees = new ArrayList<>(employeesSignal.get());
                    int index = employees.indexOf(employee);
                    employees.set(index, new Employee(
                        employee.id(), employee.name(), employee.department(),
                        EmployeeStatus.ON_LEAVE, employee.salary()
                    ));
                    employeesSignal.set(employees);
                    grid.setItems(employees);
                });
            }

            return true;
        });

        // Action buttons
        Button addButton = new Button("Add Employee", e -> {
            List<Employee> employees = new ArrayList<>(employeesSignal.get());
            employees.add(new Employee(
                "E" + (employees.size() + 1),
                "New Employee",
                "Unassigned",
                EmployeeStatus.ACTIVE,
                50000.0
            ));
            employeesSignal.set(employees);
            grid.setItems(employees);
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
