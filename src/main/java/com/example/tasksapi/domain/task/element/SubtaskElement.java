package com.example.tasksapi.domain.task.element;

import com.example.tasksapi.domain.task.Task;
import jakarta.persistence.*;

@Entity
@Table(name = "task_element_subtasks")
@DiscriminatorValue("SUBTASK")
public class SubtaskElement extends TaskElement {

    @Column(nullable = false)
    private String title;

    private boolean completed = false;

    public SubtaskElement() {}

    public SubtaskElement(String title, Task task) {
        super(task);
        this.title = title;
        this.completed = false;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
