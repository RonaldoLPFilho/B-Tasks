package com.example.tasksapi.domain.task.element;

import com.example.tasksapi.domain.Auditable;
import com.example.tasksapi.domain.task.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "task_elements")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "element_type", discriminatorType = DiscriminatorType.STRING)
public abstract class TaskElement extends Auditable {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;

    @Column(name = "element_type", insertable = false, updatable = false)
    private String elementType;

    @Column(name = "sort_order")
    private int sortOrder = 0;

    public TaskElement() {}

    public TaskElement(Task task) {
        this.task = task;
    }

    public UUID getId() { return id; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public String getElementType() { return elementType; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
