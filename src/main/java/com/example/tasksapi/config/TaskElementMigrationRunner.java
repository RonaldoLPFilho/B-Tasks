package com.example.tasksapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(10)
public class TaskElementMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TaskElementMigrationRunner.class);
    private final JdbcTemplate jdbc;

    public TaskElementMigrationRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM task_elements", Integer.class);
        if (count != null && count > 0) {
            log.info("TaskElement migration already done, skipping.");
            return;
        }

        migrateSubtasks();
        migrateComments();
        log.info("TaskElement migration completed.");
    }

    private void migrateSubtasks() {
        try {
            jdbc.execute("""
                INSERT INTO task_elements (id, task_id, element_type, sort_order, created_at, updated_at)
                SELECT id, task_id, 'SUBTASK', 0, NOW(), NOW() FROM subtask
            """);
            jdbc.execute("""
                INSERT INTO task_element_subtasks (id, title, completed)
                SELECT id, title, completed FROM subtask
            """);
            log.info("Migrated subtasks to task_element_subtasks.");
        } catch (Exception e) {
            log.warn("Subtask migration skipped: {}", e.getMessage());
        }
    }

    private void migrateComments() {
        try {
            jdbc.execute("""
                INSERT INTO task_elements (id, task_id, element_type, sort_order, created_at, updated_at)
                SELECT id, task_id, 'COMMENT', 0, NOW(), NOW() FROM comment
            """);
            jdbc.execute("""
                INSERT INTO task_element_comments (id, description, author, created_at)
                SELECT id, description, author, created_at FROM comment
            """);
            log.info("Migrated comments to task_element_comments.");
        } catch (Exception e) {
            log.warn("Comment migration skipped: {}", e.getMessage());
        }
    }
}
