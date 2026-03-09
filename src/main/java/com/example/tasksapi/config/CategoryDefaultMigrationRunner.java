package com.example.tasksapi.config;

import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.task.Category;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.repository.UserRepository;
import com.example.tasksapi.service.task.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(3)
public class CategoryDefaultMigrationRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CategoryDefaultMigrationRunner.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CategoryService categoryService;

    public CategoryDefaultMigrationRunner(
            UserRepository userRepository,
            TaskRepository taskRepository,
            CategoryService categoryService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int ensuredDefaults = 0;

        for (User user : userRepository.findAll()) {
            Category defaultCategory = categoryService.ensureDefaultCategory(user);
            List<Task> uncategorizedTasks = taskRepository.findByUserIdAndCategoryIsNull(user.getId());
            for (Task task : uncategorizedTasks) {
                task.setCategory(defaultCategory);
            }
            taskRepository.saveAll(uncategorizedTasks);
            ensuredDefaults++;
        }

        if (ensuredDefaults > 0) {
            log.info("Ensured default category for {} users", ensuredDefaults);
        }
    }
}
