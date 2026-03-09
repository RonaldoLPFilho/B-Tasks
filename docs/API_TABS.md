# API Tabs - Documentação para Frontend

Este documento descreve as alterações na API para suportar **Tabs** (abas) que agrupam tasks. As tasks passam a pertencer obrigatoriamente a uma tab.

## Resumo das Mudanças

### Novos Conceitos

- **Tab**: Agrupa tasks. Exemplo: "Atividades de casa", "Trabalho", etc.
- **Limite**: Usuário pode ter até **5 tabs ativas** simultaneamente.
- **Nome da tab**: Máximo de **20 caracteres**.
- **Arquivamento**: Tabs podem ser arquivadas (permanecem no banco, não contam no limite).
- **Remoção**: Ao remover uma tab, todas as tasks filhas são removidas em cascata.

### Sections (novo)

- **Tab** contém **Sections** (ex: "Geral", "Segunda-feira", "Revisar").
- **Section** contém **Tasks**.
- Máximo **5 sections por tab** (incluindo "Geral").
- Toda tab nova começa com a section "Geral".
- Novas tasks são criadas na section "Geral".

### Alterações em Tasks

- **Criação**: `tabId` é **obrigatório** no body (task é criada na section "Geral" da tab).
- **Listagem**: Parâmetro opcional `?tabId=uuid` para filtrar por tab. Cada task inclui `sectionId` e `tabId`.
- **Reorder**: Novo contrato com `sectionUpdates` — permite reordenar e mover tasks entre sections.

---

## Novos Endpoints: Tabs

Base path: `/api/tabs`

Autenticação: `Authorization: Bearer <token>`

### Listar tabs ativas

```
GET /api/tabs
```

Retorna apenas tabs com `archived = false`.

**Response 200:**
```json
{
  "status": "SUCCESS",
  "message": "Tabs",
  "httpStatus": "OK",
  "timestamp": "2025-03-08T12:00:00",
  "data": [
    {
      "id": "uuid",
      "name": "Atividades de casa",
      "archived": false,
      "sortOrder": 0,
      "createdAt": "2025-03-08T10:00:00",
      "updatedAt": "2025-03-08T10:00:00",
      "sections": []
    }
  ]
}
```

### Listar todas as tabs (incluindo arquivadas)

```
GET /api/tabs/all
```

**Response 200:** Mesmo formato, inclui tabs com `archived = true`.

### Obter tab por ID (com sections e tasks)

```
GET /api/tabs/{tabId}
```

**Response 200:**
```json
{
  "status": "SUCCESS",
  "message": "Tab found",
  "data": {
    "id": "uuid",
    "name": "Atividades de casa",
    "archived": false,
    "sortOrder": 0,
    "createdAt": "...",
    "updatedAt": "...",
    "sections": [
      {
        "id": "section-uuid",
        "name": "Geral",
        "sortOrder": 0,
        "tasks": [
          {
            "id": "task-uuid",
            "title": "arrumar o escritório",
            "tabId": "tab-uuid",
            "sectionId": "section-uuid",
            "completed": false,
            "subtasks": [],
            "comments": []
          }
        ]
      }
    ]
  }
}
```

### Criar tab

```
POST /api/tabs
Content-Type: application/json
```

**Request body:**
```json
{
  "name": "Atividades de casa"
}
```

| Campo | Obrigatório | Limite |
|-------|-------------|--------|
| name  | Sim         | Máx. 20 caracteres |

**Response 201:**
```json
{
  "status": "SUCCESS",
  "message": "Tab created",
  "httpStatus": "CREATED",
  "data": {
    "id": "uuid",
    "name": "Atividades de casa",
    "archived": false,
    "sortOrder": 0,
    "createdAt": "...",
    "updatedAt": "...",
    "tasks": []
  }
}
```

**Erro 400** – Limite de 5 tabs ativas excedido:
```json
{
  "status": "ERROR",
  "message": "Maximum of 5 active tabs allowed. Archive or remove a tab first.",
  "httpStatus": "BAD_REQUEST"
}
```

**Erro 400** – Nome da tab excede 20 caracteres:
```json
{
  "message": "Tab name must have at most 20 characters"
}
```

### Atualizar tab

```
PUT /api/tabs/{tabId}
Content-Type: application/json
```

**Request body (campos opcionais):**
```json
{
  "name": "Novo nome",
  "archived": false
}
```

**Response 200:** Tab atualizada.

**Erro 400** – Ao desarquivar (`archived: false`) com 5 tabs ativas:
```json
{
  "message": "Maximum of 5 active tabs allowed. Archive another tab first."
}
```

### Arquivar tab

```
PATCH /api/tabs/{tabId}/archive
```

**Response 200:** Tab arquivada.

### Desarquivar tab

```
PATCH /api/tabs/{tabId}/unarchive
```

**Response 200:** Tab desarquivada.

**Erro 400** – Limite de 5 tabs ativas:
```json
{
  "message": "Maximum of 5 active tabs allowed. Archive another tab first."
}
```

### Remover tab (e todas as tasks)

```
DELETE /api/tabs/{tabId}
Content-Type: application/json
```

Remove a tab e **todas as tasks** pertencentes a ela.

**Confirmação por senha:** Se a tab contiver tasks, o usuário deve informar a senha no body para confirmar a remoção.

**Request body (obrigatório quando a tab tem tasks):**
```json
{
  "password": "senha-do-usuario"
}
```

- **Tab vazia (sem tasks):** body opcional ou vazio.
- **Tab com tasks:** body obrigatório com `password` preenchido.

**Response 200:**
```json
{
  "status": "SUCCESS",
  "message": "Tab and its tasks removed"
}
```

**Erro 400** – Senha não informada ao remover tab com tasks:
```json
{
  "message": "Password confirmation is required to remove a tab that contains tasks"
}
```

**Erro 401** – Senha incorreta:
```json
{
  "message": "Invalid password"
}
```

---

## Endpoints: Sections

Base path: `/api/tabs/{tabId}/sections`

Autenticação: `Authorization: Bearer <token>`

### Listar sections da tab

```
GET /api/tabs/{tabId}/sections
```

**Response 200:** Lista de sections ordenadas por sortOrder, cada uma com suas tasks.

### Criar section

```
POST /api/tabs/{tabId}/sections
Content-Type: application/json
```

**Request body:**
```json
{
  "name": "Segunda-feira"
}
```

**Erro 400** – Limite de 5 sections excedido:
```json
{
  "message": "Maximum of 5 sections per tab allowed"
}
```

### Atualizar section

```
PUT /api/tabs/{tabId}/sections/{sectionId}
Content-Type: application/json
```

**Request body:**
```json
{
  "name": "Novo nome"
}
```

**Erro 400** – Ao tentar alterar a section "Geral":
```json
{
  "message": "Cannot modify the default 'Geral' section"
}
```

### Excluir section

```
DELETE /api/tabs/{tabId}/sections/{sectionId}
```

As tasks da section são movidas para "Geral". A section "Geral" não pode ser excluída nem alterada (nome fixo); o usuário pode apenas adicionar e retirar tasks dela.

**Erro 400** – Ao tentar excluir a section "Geral":
```json
{
  "message": "Cannot delete the default 'Geral' section"
}
```

### Reordenar sections

```
PATCH /api/tabs/{tabId}/sections/reorder
Content-Type: application/json
```

**Request body:**
```json
{
  "orderedIds": ["uuid-section-1", "uuid-section-2", "uuid-section-3"]
}
```

---

## Alterações em Tasks

### Criar task

```
POST /api/tasks
Content-Type: application/json
```

**Request body (contrato atualizado):**
```json
{
  "title": "arrumar o escritório",
  "description": "Organizar mesa e prateleiras",
  "completed": false,
  "tabId": "uuid-da-tab",
  "categoryId": "uuid-da-categoria-ou-null",
  "jiraId": null
}
```

| Campo       | Obrigatório | Descrição                          |
|------------|-------------|------------------------------------|
| title      | Sim         | Título da task                     |
| tabId      | Sim         | ID da tab onde a task será criada  |
| categoryId | Não         | ID da categoria; se omitido, usa a categoria padrão do usuário |
| description| Não         | Descrição                          |
| completed  | Não         | Default: false                     |
| jiraId     | Não         | Integração Jira                    |

**Erro 400** – Tab ausente:
```json
{
  "message": "Tab is required for creating a task"
}
```

### Listar tasks

```
GET /api/tasks
GET /api/tasks?tabId={uuid}
```

- Sem `tabId`: retorna todas as tasks do usuário.
- Com `tabId`: retorna apenas as tasks da tab informada.

**Response 200:** Lista de tasks. Cada task inclui `tabId` e `sectionId` na resposta:
```json
{
  "data": [
    {
      "id": "uuid",
      "title": "arrumar o escritório",
      "tabId": "uuid-da-tab",
      "sectionId": "uuid-da-section",
      "completed": false,
      "subtasks": [],
      "comments": []
    }
  ]
}
```

### Reordenar tasks (entre sections)

```
PATCH /api/tasks/reorder
Content-Type: application/json
```

**Request body (contrato atualizado):**
```json
{
  "tabId": "uuid-da-tab",
  "sectionUpdates": [
    { "sectionId": "uuid-geral", "orderedIds": ["uuid-task-1", "uuid-task-3"] },
    { "sectionId": "uuid-segunda", "orderedIds": ["uuid-task-2", "uuid-task-4"] }
  ]
}
```

| Campo | Obrigatório | Descrição |
|-------|-------------|-----------|
| tabId | Sim | ID da tab |
| sectionUpdates | Sim | Lista de { sectionId, orderedIds } — cada section com tasks na nova ordem |

Permite reordenar dentro de uma section e mover tasks entre sections (drag-and-drop).

**Erro 400** – Tab ausente:
```json
{
  "message": "Tab is required for reordering tasks"
}
```

**Erro 400** – IDs inválidos ou duplicados:
```json
{
  "message": "IDs don't belong to tab or are duplicated"
}
```

---

## Fluxos Recomendados

### Carregar interface inicial

1. `GET /api/tabs` – listar tabs ativas.
2. Para cada tab (ou tab selecionada): `GET /api/tasks?tabId={tabId}` – listar tasks.

Alternativa: `GET /api/tabs/{tabId}` – retorna tab com sections e tasks aninhadas.

### Criar nova task

1. Usuário escolhe a tab.
2. `POST /api/tasks` com `tabId` no body.

### Arquivar tab

1. `PATCH /api/tabs/{tabId}/archive`.
2. Remover tab da lista de tabs ativas na UI.

### Remover tab

1. Se a tab tiver tasks: exibir modal de confirmação solicitando a senha do usuário.
2. `DELETE /api/tabs/{tabId}` com body `{ "password": "..." }` quando a tab contiver tasks.
3. Se a tab estiver vazia: body opcional.

### Reordenar tasks dentro e entre sections

1. `PATCH /api/tasks/reorder` com `{ tabId, sectionUpdates: [{ sectionId, orderedIds }] }`.

---

## Códigos de Erro Comuns

| HTTP | Mensagem típica |
|------|------------------|
| 400 | Tab is required for creating a task |
| 400 | Tab is required for reordering tasks |
| 400 | Maximum of 5 active tabs allowed. Archive or remove a tab first. |
| 400 | Tab name is required |
| 400 | Tab name must have at most 20 characters |
| 400 | Password confirmation is required to remove a tab that contains tasks |
| 401 | Invalid password |
| 404 | Tab not found with id {id} |
| 404 | Task not found with id {id} |

---

## Endpoints: Categories

Base path: `/api/categories`

### Listar categorias do usuário

```
GET /api/categories
```

Retorna as categorias do usuário com a categoria padrão destacada por `defaultCategory = true`.

### Criar categoria

```
POST /api/categories
Content-Type: application/json
```

**Request body:**
```json
{
  "name": "Urgente",
  "color": "#FF0000"
}
```

### Atualizar categoria

```
PUT /api/categories/{categoryId}
Content-Type: application/json
```

**Request body:** mesmo contrato da criação.

### Remover categoria

```
DELETE /api/categories/{categoryId}
Content-Type: application/json
```

- Ao remover uma categoria comum, as tasks vinculadas são movidas para a categoria padrão do usuário.
- Ao remover a categoria padrão, é obrigatório informar outra categoria do mesmo usuário para assumir como nova padrão.

**Request body para remover a categoria padrão:**
```json
{
  "replacementCategoryId": "uuid-da-nova-categoria-padrao"
}
```

---

## Endpoints: Pomodoro

Base path: `/api/pomodoro`

### Preferências do Pomodoro

```
GET /api/pomodoro
PUT /api/pomodoro
GET /api/pomodoro/sounds
```

### Estado persistido do timer

Base path: `/api/pomodoro/state`

```
GET /api/pomodoro/state
POST /api/pomodoro/state/start
POST /api/pomodoro/state/pause
POST /api/pomodoro/state/resume
POST /api/pomodoro/state/reset
POST /api/pomodoro/state/acknowledge
```

- O backend persiste modo, status, tempo restante e timestamps do ciclo atual.
- O frontend continua responsável pela contagem visual e por tocar o som do alarme, mas sincroniza o estado com a API.

---

## Migração de Dados Existentes

Na primeira execução após o deploy:
1. Tasks existentes **sem tab** são migradas para uma tab padrão "Tarefas gerais" por usuário.
2. Para cada tab existente, é criada a section "Geral" e as tasks são associadas a ela.
3. Nenhuma ação é necessária no frontend.
