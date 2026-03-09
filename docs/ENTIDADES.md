# Entidades do Projeto B-Tasks

Documentação das entidades do domínio, suas características e regras de negócio.

---

## Diagrama de Relacionamentos

```mermaid
erDiagram
    User ||--o{ Tab : owns
    User ||--o{ Task : owns
    User ||--o{ Category : owns
    User ||--|| PomodoroPreferences : has
    User ||--|| UserLanguagePreference : has
    User ||--o| PasswordResetToken : has
    
    Tab ||--o{ Section : contains
    Section ||--o{ Task : contains
    Task }o--|| Category : "optional"
    Task ||--o{ Subtask : has
    Task ||--o{ Comment : has
    
    Auditable }|-- User : extends
    Auditable }|-- Task : extends
    Auditable }|-- Tab : extends
    Auditable }|-- Section : extends
```

---

## Classe Base: Auditable

**Tipo:** `@MappedSuperclass` (não é entidade persistida diretamente)

**Descrição:** Fornece campos de auditoria para entidades que a estendem.

| Campo      | Tipo           | Descrição                    |
|------------|----------------|------------------------------|
| createdAt  | LocalDateTime  | Data/hora de criação (não atualizável) |
| updatedAt  | LocalDateTime  | Data/hora da última atualização       |

**Entidades que estendem:** User, Task, Tab, Section

---

## User

**Tabela:** `users`

**Descrição:** Usuário do sistema. Entidade central de autenticação e associação.

| Campo   | Tipo    | Obrigatório | Restrições        |
|---------|---------|-------------|-------------------|
| id      | UUID    | Sim         | PK, gerado        |
| username| String  | Sim         | Único             |
| email   | String  | Sim         | Único             |
| password| String  | Não         | Hash BCrypt       |
| createdAt | LocalDateTime | -    | Herdado de Auditable |
| updatedAt | LocalDateTime | -    | Herdado de Auditable |

**Relacionamentos:**
- **1:N** com Task (tasks)
- **1:N** com Category (categories)
- **1:1** com PomodoroPreferences
- **1:1** com UserLanguagePreference
- **1:1** com PasswordResetToken (temporário)

---

## Tab

**Tabela:** `tab`

**Descrição:** Aba que agrupa tasks. Permite organizar tarefas por contexto (ex: "Atividades de casa", "Trabalho").

| Campo     | Tipo    | Obrigatório | Restrições        |
|-----------|---------|-------------|-------------------|
| id        | UUID    | Sim         | PK, gerado        |
| name      | String  | Sim         | Máx. 20 caracteres |
| user_id   | UUID    | Sim         | FK para User      |
| archived  | boolean | Sim         | Default: false    |
| sort_order| Integer | Sim         | Ordenação das tabs |
| createdAt | LocalDateTime | -  | Herdado de Auditable |
| updatedAt | LocalDateTime | -  | Herdado de Auditable |

**Regras de negócio:**
- Usuário pode ter **até 5 tabs ativas** (archived = false)
- Tabs arquivadas não contam no limite
- Nome limitado a **20 caracteres**
- Ao remover tab com tasks: exige confirmação por senha
- Ao remover tab: todas as tasks filhas são removidas em cascata

**Relacionamentos:**
- **N:1** com User (dono)
- **1:N** com Section (sections contidas)

---

## Section

**Tabela:** `section`

**Descrição:** Seção dentro de uma tab. Permite organizar tasks em grupos (ex: "Geral", "Segunda-feira", "Revisar").
Toda tab nova começa com a section "Geral". Novas tasks são criadas na section "Geral".

| Campo | Tipo | Obrigatório | Restrições |
|-------|------|-------------|------------|
| id | UUID | Sim | PK, gerado |
| name | String | Sim | - |
| tab_id | UUID | Sim | FK para Tab |
| sort_order | Integer | Sim | Ordenação dentro da tab |
| createdAt | LocalDateTime | - | Herdado de Auditable |
| updatedAt | LocalDateTime | - | Herdado de Auditable |

**Regras de negócio:**
- Máximo **5 sections por tab** (incluindo "Geral")
- Section "Geral" é criada automaticamente ao criar uma tab
- Section "Geral" **não pode ser removida nem alterada** (nome fixo); o usuário pode apenas adicionar e retirar tasks dela
- Ao excluir uma section não vazia: tasks são movidas para "Geral"

**Relacionamentos:**
- **N:1** com Tab (obrigatório)
- **1:N** com Task (tasks contidas)

---

## Task

**Tabela:** `task`

**Descrição:** Tarefa principal do sistema. Pertence a uma tab e opcionalmente a uma categoria.

| Campo      | Tipo    | Obrigatório | Restrições                    |
|------------|---------|-------------|-------------------------------|
| id         | UUID    | Sim         | PK, gerado                    |
| title      | String  | Sim         | -                             |
| description| String  | Não         | -                             |
| category_id| UUID    | Não         | FK para Category (opcional)  |
| section_id | UUID    | Sim         | FK para Section               |
| user_id    | UUID    | Sim         | FK para User                  |
| completed  | boolean | Sim         | Default: false                |
| finishedAt | LocalDate | Não       | Data de conclusão             |
| jiraId     | String  | Não         | Integração Jira               |
| sort_order | Integer | Sim         | Único por section (ux_task_section_sort) |
| active     | boolean | Sim         | Default: true (soft delete)   |
| createdAt  | LocalDateTime | -    | Herdado de Auditable          |
| updatedAt  | LocalDateTime | -    | Herdado de Auditable          |

**Constraints:**
- `ux_task_section_sort`: (section_id, sort_order) único por section

**Regras de negócio:**
- Obrigatoriamente pertence a uma **section** (que pertence a uma tab)
- `sort_order` define a ordem dentro da section
- Novas tasks são criadas na section "Geral" da tab informada
- `active = false`: task desativada (soft delete); não filtra listagens atualmente
- Ao criar: `tabId` obrigatório no request

**Relacionamentos:**
- **N:1** com User (dono)
- **N:1** com Section (obrigatório)
- **N:1** com Category (opcional)
- **1:N** com Subtask (cascade, orphanRemoval)
- **1:N** com Comment (cascade, orphanRemoval)

---

## Category

**Tabela:** `category`

**Descrição:** Categoria para classificação/tagging de tasks (ex: cor, tipo). Independente de Tab.

| Campo   | Tipo   | Obrigatório | Restrições        |
|---------|--------|-------------|-------------------|
| id      | UUID   | Sim         | PK, gerado        |
| name    | String | Sim         | Único por usuário |
| color   | String | Sim         | Formato hex (#RRGGBB ou #RGB) |
| user_id | UUID   | Sim         | FK para User      |

**Validação:** `@HexColor` em `color`

**Relacionamentos:**
- **N:1** com User (dono)
- **1:N** com Task (tasks que usam a categoria)

---

## Subtask

**Tabela:** (nome implícito `subtask`)

**Descrição:** Subtarefa vinculada a uma task. Removida em cascata com a task.

| Campo    | Tipo   | Obrigatório | Restrições |
|----------|--------|-------------|------------|
| id       | UUID   | Sim         | PK, gerado |
| title    | String | Não         | -          |
| completed| boolean| Sim         | Default: false |
| task_id  | UUID   | Sim         | FK para Task |

**Relacionamentos:**
- **N:1** com Task (cascade ALL, orphanRemoval)

---

## Comment

**Tabela:** (nome implícito `comment`)

**Descrição:** Comentário em uma task. Autor é String (não vinculado a User).

| Campo      | Tipo           | Obrigatório | Restrições |
|------------|----------------|------------|------------|
| id         | UUID           | Sim        | PK, gerado |
| description| String         | Não        | -          |
| author     | String         | Não        | -          |
| createdAt  | LocalDateTime  | Não        | Preenchido no construtor |
| task_id    | UUID           | Sim        | FK para Task |

**Relacionamentos:**
- **N:1** com Task (cascade ALL, orphanRemoval)

---

## PomodoroPreferences

**Tabela:** `pomodoro_preferences`

**Descrição:** Preferências do timer Pomodoro por usuário.

| Campo          | Tipo   | Obrigatório | Restrições |
|----------------|--------|-------------|------------|
| id             | UUID   | Sim         | PK, gerado |
| sessionDuration| int    | Não         | Duração da sessão (min) |
| breakDuration  | int    | Não         | Duração do intervalo (min) |
| alarmSound     | String | Não         | Nome do som do alarme |
| user_id        | UUID   | Sim         | FK para User (OneToOne) |

**Relacionamentos:**
- **1:1** com User

---

## UserLanguagePreference

**Tabela:** `user_language_preferences`

**Descrição:** Preferência de idioma do usuário.

| Campo    | Tipo           | Obrigatório | Restrições |
|----------|----------------|-------------|------------|
| id       | UUID           | Sim         | PK, gerado |
| language | LanguageOption | Sim         | Enum: PT_BR, EN_US, ES_ES |
| user_id  | UUID           | Sim         | FK para User (OneToOne) |

**Relacionamentos:**
- **1:1** com User

---

## PasswordResetToken

**Tabela:** (nome implícito `password_reset_token`)

**Descrição:** Token temporário para recuperação de senha.

| Campo         | Tipo           | Obrigatório | Restrições |
|---------------|----------------|-------------|------------|
| id            | UUID           | Sim         | PK, gerado |
| token         | String         | Não         | -          |
| user_id       | UUID           | Sim         | FK para User |
| expirationDate| LocalDateTime  | Não         | Validade do token |

**Relacionamentos:**
- **1:1** com User

---

## Contratos e Validações

### HexColor

**Anotação:** `@HexColor`

**Aplicação:** Campo `color` da entidade Category

**Formato aceito:** `#RRGGBB` ou `#RGB` (hexadecimal)

**Mensagem de erro:** "Invalid hex color, use hexadecimal format (#RRGGBB or #RGB)"

### LanguageOption (enum)

| Valor  | Display Name        |
|--------|---------------------|
| PT_BR  | Português Brasileiro |
| EN_US  | English             |
| ES_ES  | Español             |

---

## Resumo de Regras por Entidade

| Entidade | Regras principais |
|----------|-------------------|
| **Tab**  | Máx. 5 ativas por usuário; nome máx. 20 caracteres; senha obrigatória para remover tab com tasks |
| **Section** | Máx. 5 por tab; "Geral" é default, imutável (não pode excluir/alterar); ao excluir section, tasks vão para "Geral" |
| **Task** | Obrigatório tabId na criação (task vai para section "Geral"); sortOrder único por section; active para soft delete |
| **Category** | Nome único por usuário; color em hex |
| **User** | username e email únicos |
