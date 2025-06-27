package com.example.tasksapi.service;

import com.example.tasksapi.domain.Task;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.OpenAiHttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public String generateDailySummary(String token) {
        try {
            List<ChatMessage> messages = new ArrayList<>();

            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
                "Você é um assistente que ajuda a preparar resumos para daily meetings. " +
                "Seu objetivo é analisar as tarefas, verificar os status, os detalhes, e então criar um resumo conciso e profissional, traduzido para o idioma escolhido" +
                "Seu resumo pode ser usado na daily meeting."));

            List<Task> tasks = taskService.findAllByToken(token);
            LocalDateTime today = LocalDateTime.now();
//
//            List<Task> todayTasks = tasks.stream()
//                .filter(task -> task.getCreateDate() != null &&
//                              task.getCreateDate().toLocalDate().equals(today.toLocalDate()))
//                .toList();

            StringBuilder prompt = new StringBuilder();
            prompt.append("Por favor, analise as seguintes tarefas do dia ")
                  .append(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                  .append(" e crie um resumo para a daily meeting:\n\n");

//            for (Task task : todayTasks) {
//                prompt.append("- Título: ").append(task.getTitle())
//                      .append("\n  Descrição: ").append(task.getDescription())
//                      .append("\n  Status: ").append(task.isCompleted() ? "Concluída" : "Pendente")
//                      .append("\n  Jira ID: ").append(task.getJiraId() != null ? task.getJiraId() : "N/A")
//                      .append("\n\n");
//            }

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
} 