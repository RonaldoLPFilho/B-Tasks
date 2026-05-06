package com.example.tasksapi.domain.task.element;

import com.example.tasksapi.domain.task.Task;
import jakarta.persistence.*;

@Entity
@Table(name = "task_element_comments")
@DiscriminatorValue("COMMENT")
public class CommentElement extends TaskElement {

    @Column(columnDefinition = "TEXT")
    private String description;

    private String author;

    public CommentElement() {}

    public CommentElement(String description, String author, Task task) {
        super(task);
        this.description = description;
        this.author = author;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
