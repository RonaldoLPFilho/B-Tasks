package com.example.tasksapi.config;

import com.example.tasksapi.domain.User;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Migra tasks existentes sem tab para uma tab padrão "Tarefas gerais".
 * Executa na inicialização da aplicação.
 */
@Component
@Order(1)
public class TabMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TabMigrationRunner.class);
    private static final String DEFAULT_TAB_NAME = "Tarefas gerais";
    private static final String DEFAULT_SECTION_NAME = "Geral";
    private static final int MAX_ACTIVE_TABS = 5;

    private final TaskRepository taskRepository;
    private final TabRepository tabRepository;
    private final SectionRepository sectionRepository;

    public TabMigrationRunner(TaskRepository taskRepository, TabRepository tabRepository,
                              SectionRepository sectionRepository) {
        this.taskRepository = taskRepository;
        this.tabRepository = tabRepository;
        this.sectionRepository = sectionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Task> tasksWithoutTab = taskRepository.findByTabIsNullAndSectionIsNull();
        if (tasksWithoutTab.isEmpty()) {
            return;
        }

        log.info("Migrating {} tasks without tab to default tab", tasksWithoutTab.size());

        Map<User, List<Task>> tasksByUser = tasksWithoutTab.stream()
                .collect(Collectors.groupingBy(Task::getUser));

        for (Map.Entry<User, List<Task>> entry : tasksByUser.entrySet()) {
            User user = entry.getKey();
            List<Task> userTasks = entry.getValue();
            migrateUserTasks(user, userTasks);
        }
    }

    private void migrateUserTasks(User user, List<Task> tasks) {
        Tab targetTab = resolveTargetTab(user);
        Section geralSection = getOrCreateGeralSection(targetTab);
        int sortOrder = 0;
        for (Task task : tasks) {
            task.setSection(geralSection);
            task.setTab(null);
            task.setSortOrder(sortOrder++);
            taskRepository.save(task);
        }

        long activeCount = tabRepository.countByUserIdAndArchivedFalse(user.getId());
        if (activeCount > MAX_ACTIVE_TABS) {
            List<Tab> activeTabs = tabRepository.findByUserIdAndArchivedFalseOrderBySortOrderAsc(user.getId());
            int archived = 0;
            int toArchive = (int) (activeCount - MAX_ACTIVE_TABS);
            for (Tab tab : activeTabs) {
                if (archived >= toArchive) break;
                if (!tab.getId().equals(targetTab.getId())) {
                    tab.setArchived(true);
                    tabRepository.save(tab);
                    archived++;
                }
            }
        }

        log.info("Migrated {} legacy tasks for user {} to tab '{}'", tasks.size(), user.getEmail(), targetTab.getName());
    }

    private Tab resolveTargetTab(User user) {
        List<Tab> userTabs = tabRepository.findByUserIdOrderBySortOrderAsc(user.getId());
        if (!userTabs.isEmpty()) {
            for (Tab tab : userTabs) {
                if (DEFAULT_TAB_NAME.equalsIgnoreCase(tab.getName())) {
                    return tab;
                }
            }

            for (Tab tab : userTabs) {
                if (!tab.isArchived()) {
                    return tab;
                }
            }

            return userTabs.get(0);
        }

        return createDefaultTab(user);
    }

    private Tab createDefaultTab(User user) {
        Tab tab = new Tab();
        tab.setName(DEFAULT_TAB_NAME);
        tab.setUser(user);
        tab.setArchived(false);
        tab.setSortOrder((int) tabRepository.countByUserIdAndArchivedFalse(user.getId()));
        return tabRepository.save(tab);
    }

    private Section getOrCreateGeralSection(Tab tab) {
        return sectionRepository.findByTabIdAndNameIgnoreCase(tab.getId(), DEFAULT_SECTION_NAME)
                .orElseGet(() -> {
                    Section section = new Section(DEFAULT_SECTION_NAME, tab, 0);
                    section = sectionRepository.save(section);
                    tab.getSections().add(section);
                    return section;
                });
    }
}
