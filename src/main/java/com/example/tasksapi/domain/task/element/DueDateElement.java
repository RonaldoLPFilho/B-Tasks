package com.example.tasksapi.domain.task.element;

import com.example.tasksapi.domain.task.Task;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "task_element_due_dates")
@DiscriminatorValue("DUE_DATE")
public class DueDateElement extends TaskElement {

    private LocalDate dueDate;

    public DueDateElement() {}

    public DueDateElement(LocalDate dueDate, Task task) {
        super(task);
        this.dueDate = dueDate;
    }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
