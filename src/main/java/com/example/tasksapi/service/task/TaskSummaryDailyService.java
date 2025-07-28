package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.task.Task;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.OpenAiHttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskSummaryDailyService {

    private final OpenAiService openAiService;
    private final TaskService taskService;

    public TaskSummaryDailyService(@Value("${openai.api.key}") String apiKey, TaskService taskService) {
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

            prompt.append("Você é um assistente que gera resumos diários com base em tarefas fornecidas. ")
                    .append("A resposta **deve ser no idioma: ").append(language).append("**.\n")
                    .append("Você **deve seguir fielmente os dados fornecidos, e caso as terefas titulo e description estejam em outro idioma, traduza para o idioma selecionado** e gerar uma saída no formato abaixo:\n\n")

                    .append("=== FORMATO ESPERADO ===\n")
                    .append("Feito:\n")
                    .append("- Ontem trabalhei na task X, onde fiz Y e falei com Z (se presente na descrição).\n\n")
                    .append("Plano do dia:\n")
                    .append("- Hoje pretendo concluir ou continuar a task W com foco em XYZ.\n")
                    .append("=== FIM DO FORMATO ===\n\n")

                    .append("Abaixo estão as tarefas separadas por categoria. Use essas informações para gerar o resumo:\n\n");

            if (!createdYestardayAndCompleted.isEmpty()) {
                prompt.append("🟢 Tarefas criadas ontem e finalizadas:\n");
                createdYestardayAndCompleted.forEach(task -> appendTask(task, prompt));
            }
            if (!createdYestardayPending.isEmpty()) {
                prompt.append("🟡 Tarefas criadas ontem e pendentes:\n");
                createdYestardayPending.forEach(task -> appendTask(task, prompt));
            }
            if (!createdTodayAndCompleted.isEmpty()) {
                prompt.append("🟢 Tarefas criadas hoje e finalizadas:\n");
                createdTodayAndCompleted.forEach(task -> appendTask(task, prompt));
            }
            if (!createdTodayPending.isEmpty()) {
                prompt.append("🟡 Tarefas criadas hoje e pendentes:\n");
                createdTodayPending.forEach(task -> appendTask(task, prompt));
            }
            if (!pastPending.isEmpty()) {
                prompt.append("🔴 Tarefas antigas ainda não finalizadas:\n");
                pastPending.forEach(task -> appendTask(task, prompt));
            }

            prompt.append("\n⚠️ Não invente informações. Use apenas o que está listado.");
            prompt.append("\n⚠️ O tom deve ser profissional, claro e objetivo.");

            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt.toString()));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
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