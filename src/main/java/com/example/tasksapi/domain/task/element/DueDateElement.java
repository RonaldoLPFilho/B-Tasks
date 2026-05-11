package com.example.tasksapi.domain.task.element;

import com.example.tasksapi.domain.task.Task;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "task_element_due_dates")
@DiscriminatorValue("DUE_DATE")
public class DueDateElement extends TaskElement {

    private LocalDate dueDate;

    private LocalTime dueTime;

    public DueDateElement() {}

    public DueDateElement(LocalDate dueDate, LocalTime dueTime, Task task) {
        super(task);
        this.dueDate = dueDate;
        this.dueTime = dueTime;
    }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalTime getDueTime() { return dueTime; }
    public void setDueTime(LocalTime dueTime) { this.dueTime = dueTime; }
}
