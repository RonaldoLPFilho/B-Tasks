package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.task.Category;
import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.SectionTaskReorderRequest;
import com.example.tasksapi.dto.TaskDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TabService tabService;
    private final SectionService sectionService;

    public TaskService(TaskRepository taskRepository, UserService userService, CategoryService categoryService,
                       TabService tabService, SectionService sectionService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.tabService = tabService;
        this.sectionService = sectionService;
    }

    public List<Task> findAllByToken(String token){
        User user = extractUser(token);
        return taskRepository.findByUserIdOrderBySortOrderAsc(user.getId());
    }

    public List<Task> findByTabId(UUID tabId, String token) {
        User user = extractUser(token);
        tabService.findByIdAndValidateOwnership(tabId, user.getId());

        List<Task> bySection = taskRepository.findBySection_Tab_IdOrderBySortOrderAsc(tabId);
        if (!bySection.isEmpty()) {
            return bySection;
        }
        return taskRepository.findByTab_IdOrderBySortOrderAsc(tabId);
    }

    public Task findById(UUID id, String token){
        User user = extractUser(token);
        return findByIdAndValidateOwnership(id, user.getId());
    }

    public Task findById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id " + id));
    }

    @Transactional
    public Task save(TaskDTO dto, String token){
        User user = extractUser(token);

        if (dto.getTabId() == null) {
            throw new InvalidDataException("Tab is required for creating a task");
        }

        tabService.findByIdAndValidateOwnership(dto.getTabId(), user.getId());
        Section geralSection = sectionService.findGeralSectionByTabId(dto.getTabId(), token);

        Category category = resolveTaskCategory(dto.getCategoryId(), user);

        Task task = new Task(dto.getTitle(), dto.getDescription(), user, geralSection, dto.getJiraId(), category);

        if(!isValidTask(task)){
            throw new InvalidDataException("Invalid task");
        }

        taskRepository.bumpAllOrdersBySectionId(geralSection.getId());
        task.setSortOrder(0);

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteById(UUID id, String token){
        User user = extractUser(token);
        Task task = findByIdAndValidateOwnership(id, user.getId());
        taskRepository.delete(task);
    }

    @Transactional
    public Task update(UUID taskId, TaskDTO dto, String token){
        User user = extractUser(token);
        Task task = findByIdAndValidateOwnership(taskId, user.getId());

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if(dto.isCompleted()){
            task.setCompleted(true);
            task.setFinishDate(LocalDate.now());
        }
        else{
            task.setCompleted(false);
            task.setFinishDate(null);
        }


        task.setJiraId(dto.getJiraId());
        task.setCategory(resolveTaskCategory(dto.getCategoryId(), user));
        return taskRepository.save(task);
    }

    @Transactional
    public void updateCompleted(UUID taskId, boolean completed, String token){
        User user = extractUser(token);
        Task task = findByIdAndValidateOwnership(taskId, user.getId());
        task.setCompleted(completed);

        if(completed){
            task.setFinishDate(LocalDate.now());
        }
        else {
            task.setFinishDate(null);
        }

        taskRepository.save(task);
    }

    public boolean existsById(UUID id){
        return taskRepository.existsById(id);
    }

    @Transactional
    public void reorder(String token, SectionTaskReorderRequest request) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.tabId() == null) {
            throw new InvalidDataException("Tab is required for reordering tasks");
        }

        tabService.findByIdAndValidateOwnership(request.tabId(), user.getId());

        if (request.sectionUpdates() == null || request.sectionUpdates().isEmpty()) {
            throw new InvalidDataException("Section updates are required");
        }

        Set<UUID> allTabTaskIds = new HashSet<>();
        for (Section s : sectionService.findByTabId(request.tabId(), token)) {
            allTabTaskIds.addAll(taskRepository.listIdsBySectionId(s.getId()));
        }
        allTabTaskIds.addAll(taskRepository.listIdsByTabId(request.tabId()));
        Set<UUID> receivedIds = new HashSet<>();
        for (SectionTaskReorderRequest.SectionUpdate update : request.sectionUpdates()) {
            for (UUID id : update.orderedIds()) {
                if (!allTabTaskIds.contains(id)) {
                    throw new InvalidDataException("IDs don't belong to tab");
                }
                if (!receivedIds.add(id)) {
                    throw new InvalidDataException("Task IDs must not be duplicated across sections");
                }
            }
        }
        if (!receivedIds.equals(allTabTaskIds)) {
            throw new InvalidDataException("All tasks must be assigned to a section");
        }

        Map<UUID, Task> tasksById = taskRepository.findAllById(receivedIds).stream()
                .collect(java.util.stream.Collectors.toMap(Task::getId, Function.identity()));
        Map<UUID, Section> sectionsById = new HashMap<>();

        int tempOrder = -receivedIds.size();
        for (SectionTaskReorderRequest.SectionUpdate update : request.sectionUpdates()) {
            Section section = sectionsById.computeIfAbsent(
                    update.sectionId(),
                    sectionId -> sectionService.findByIdAndValidateTab(sectionId, request.tabId(), user.getId())
            );

            for (UUID taskId : update.orderedIds()) {
                Task task = tasksById.get(taskId);
                if (task == null) {
                    throw new InvalidDataException("Task not found for reordering");
                }

                task.setSection(section);
                task.setSortOrder(tempOrder++);
            }
        }

        taskRepository.saveAll(tasksById.values());
        taskRepository.flush();

        for (SectionTaskReorderRequest.SectionUpdate update : request.sectionUpdates()) {
            for (int i = 0; i < update.orderedIds().size(); i++) {
                Task task = tasksById.get(update.orderedIds().get(i));
                task.setSortOrder(i);
            }
        }

        taskRepository.saveAll(tasksById.values());
        taskRepository.flush();
    }

    @Transactional
    public void disableTask(UUID taskId, String token){
        User user = extractUser(token);
        Task task = findByIdAndValidateOwnership(taskId, user.getId());
        task.setActive(false);

        taskRepository.save(task);
    }


    @Transactional
    public void activeTask(UUID taskId, String token){
        User user = extractUser(token);
        Task task = findByIdAndValidateOwnership(taskId, user.getId());
        task.setActive(true);

        taskRepository.save(task);
    }

    private boolean isValidTask(Task task){
        return task.getTitle() != null &&
                !task.getTitle().isBlank();
    }

    private User extractUser(String token) {
        return userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Task findByIdAndValidateOwnership(UUID id, UUID userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id " + id));

        if (!task.getUser().getId().equals(userId)) {
            throw new NotFoundException("Task not found with id " + id);
        }

        return task;
    }

    private Category resolveTaskCategory(UUID categoryId, User user) {
        if (categoryId == null) {
            return categoryService.ensureDefaultCategory(user);
        }

        return categoryService.findByIdAndValidateOwnership(categoryId, user.getId());
    }
}
