package com.example.tasksapi.config;

import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.repository.SectionRepository;
import com.example.tasksapi.repository.TabRepository;
import com.example.tasksapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Migra tasks existentes (com tab) para sections.
 * Para cada tab, cria Section "Geral" e associa as tasks a ela.
 * Executa após TabMigrationRunner.
 */
@Component
@Order(2)
public class SectionMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SectionMigrationRunner.class);
    public static final String DEFAULT_SECTION_NAME = "Geral";

    private final TabRepository tabRepository;
    private final SectionRepository sectionRepository;
    private final TaskRepository taskRepository;

    public SectionMigrationRunner(TabRepository tabRepository, SectionRepository sectionRepository,
                                 TaskRepository taskRepository) {
        this.tabRepository = tabRepository;
        this.sectionRepository = sectionRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Tab> allTabs = tabRepository.findAll();
        int migratedCount = 0;

        for (Tab tab : allTabs) {
            Section geralSection = getOrCreateGeralSection(tab);
            List<Task> tasksInTab = taskRepository.findByTab_IdAndSectionIsNullOrderBySortOrderAsc(tab.getId());

            for (Task task : tasksInTab) {
                task.setSection(geralSection);
                task.setTab(null);
                taskRepository.save(task);
                migratedCount++;
            }
        }

        if (migratedCount > 0) {
            log.info("Migrated {} tasks to sections (Geral)", migratedCount);
        }
    }

    private Section getOrCreateGeralSection(Tab tab) {
        return sectionRepository.findByTabIdAndNameIgnoreCase(tab.getId(), DEFAULT_SECTION_NAME)
                .orElseGet(() -> {
                    Section section = new Section(DEFAULT_SECTION_NAME, tab, 0);
                    section = sectionRepository.save(section);
                    tab.getSections().add(section);
                    log.debug("Created section '{}' for tab {}", DEFAULT_SECTION_NAME, tab.getId());
                    return section;
                });
    }
}
