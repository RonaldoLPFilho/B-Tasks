package com.example.tasksapi.service;

import com.example.tasksapi.domain.Task;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.OpenAiHttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiService openAiService;
    private final TaskService taskService;

    public OpenAIService(@Value("${openai.api.key}") String apiKey, TaskService taskService) {
        this.openAiService = new OpenAiService(apiKey);
        this.taskService = taskService;
    }

    public String generateDailySummary(String token, String language) {
        try {
            List<ChatMessage> messages = new ArrayList<>();

            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
                "Você é um assistente que ajuda a preparar resumos para daily meetings. " +
                "Seu objetivo é analisar as tarefas, verificar os status, os detalhes, e então criar um resumo conciso e profissional, traduzido para o idioma escolhido" +
                "Seu resumo pode ser usado na daily meeting."));

//            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(),
//                    "Você é um assistente que prepara resumos para reuniões diárias (daily meeting)." +
//                            "Você deve gerar um resumo bem estruturado, traduzido para o idioma solicitado pelo usuário, com base nas tarefas listadas."));

            List<Task> tasks = taskService.findAllByToken(token);
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            List<Task> createdYestardayAndCompleted = new ArrayList<>();
            List<Task> createdYestardayPending = new ArrayList<>();
            List<Task> createdTodayAndCompleted = new ArrayList<>();
            List<Task> createdTodayPending = new ArrayList<>();
            List<Task> pastPending = new ArrayList<>();

            for (Task task : tasks) {
                LocalDate created = task.getCreatedAt().toLocalDate();
                boolean isCompleted = task.isCompleted();

                if(created.equals(yesterday)){
                    if(isCompleted) createdYestardayAndCompleted.add(task);
                    else createdYestardayPending.add(task);
                }else if(created.equals(today)){
                    if(isCompleted) createdTodayAndCompleted.add(task);
                    else createdTodayPending.add(task);
                }else if (created.isBefore(yesterday) && !isCompleted) {
                    pastPending.add(task);
                }
            }

            StringBuilder prompt = new StringBuilder();

            prompt.append("Idioma: ").append(language).append("\n\n");
            prompt.append("Por favor, gere um resumo da reunião diária com base nas seguintes tarefas: ");

            if (!createdYestardayAndCompleted.isEmpty()) {
                prompt.append("Tarefas criadas ontem e finalizadas:\n");
                createdYestardayAndCompleted.forEach(task -> appendTask(task, prompt));
            }

            if(!createdYestardayPending.isEmpty()){
                prompt.append("Tarefas criadas ontem e ainda não foram concluídas:\n");
                createdYestardayPending.forEach(task -> appendTask(task, prompt));
            }

            if(!createdTodayAndCompleted.isEmpty()){
                prompt.append("Tarefas criadas hoje e finalizadas:\n");
                createdTodayAndCompleted.forEach(task -> appendTask(task, prompt));
            }

            if(!createdTodayPending.isEmpty()){
                prompt.append("Tarefas criadas hoje e pendentes: \n");
                createdTodayPending.forEach(task -> appendTask(task, prompt));
            }

            if(!pastPending.isEmpty()){
                prompt.append("Tarefas antigas e ainda não finalizadas: \n");
                pastPending.forEach(task -> appendTask(task, prompt));
            }

            prompt.append("\nFormato de resposta esperado:\n\n");
            prompt.append("Feito:\n");
            prompt.append("Ontem trabalhei na task xxx, falei com xxx (obtido na description), e consegui avançar bem na tarefa...\n\n");
            prompt.append("Plano do dia:\n");
            prompt.append("Hoje vou seguir com a tarefa xxx, com o objetivo de xxx\n");

            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt.toString()));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .build();

            return openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();

        } catch (OpenAiHttpException e) {
            if (e.statusCode == 429) {
                throw new RuntimeException("Limite de uso da API OpenAI excedido. Por favor, verifique sua cota e faturamento em https://platform.openai.com/account/usage");
            }
            throw new RuntimeException("Erro ao gerar resumo: " + e.getMessage());
        }
    }

    private void appendTask(Task task, StringBuilder prompt) {
        prompt.append("- Título: ").append(task.getTitle()).append("\n");
        prompt.append("  Descrição: ").append(task.getDescription() != null ? task.getDescription() : "N/A").append("\n");
        prompt.append("  Jira ID: ").append(task.getJiraId() != null ? task.getJiraId() : "N/A").append("\n\n");
    }
} 