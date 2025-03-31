package com.example.tasksapi.controller;

import com.example.tasksapi.domain.Task;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.findAll());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.findById(taskId));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTaskById(@PathVariable Long taskId) {
        taskService.deleteById(taskId);
        return ResponseEntity.ok("Deleted task with id " + taskId);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskDTO dto) {
        return ResponseEntity.ok(taskService.save(dto));
    }

    @PutMapping("{/taskId")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody TaskDTO dto) {
        return ResponseEntity.ok(taskService.update(taskId, dto));
    }
}
