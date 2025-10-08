**[AINDA EM DESENVOLVIMENTO - NOVAS FEATURES SENDO IMPLEMENTADAS, WIKI DESATUALIZADA]**


📝 Projeto: Tasks App – Gerenciador de Tarefas com Autenticação

Projeto de estudos práticos:
- Este projeto é uma aplicação web completa de gerenciamento de tarefas.

--- 
Tecnologias utilizadas

[Backend (Java + Spring Boot):](https://github.com/RonaldoLPFilho/B-Tasks)

    Spring Boot 3

    Spring Security com JWT

    JPA + Hibernate

    PostgreSQL

    Jakarta Mail (envio de e-mails)

    Maven

[Frontend (React + Vite):](https://github.com/RonaldoLPFilho/F-Tasks)

    React + TypeScript

    Tailwind CSS

    Axios

    React Router DOM

    Context API (para auth global)

  ---

**Funcionalidades:**

Autenticação

- Registro de novo usuário

- Login com autenticação via token JWT

- Armazenamento seguro do token

- Logout

Recuperação de senha

- Envio de link de redefinição por e-mail

- Token com validade de 15 minutos

- Formulário para criação de nova senha

- Validação do token (expirado ou já utilizado)

Gerenciamento de tarefas

- Listagem de tarefas por usuário

- Criação de tarefas com título e descrição (opcional)

- Marcar tarefas como concluídas

- Editar título, descrição e status

- Remover tarefas

Interface personalizada para o usuário logado

- Header com nome do usuário e botão de logout

- Proteção de rotas com redirecionamento para login se o token for inválido

  ---

⚙️ Como rodar o projeto

Pré-requisitos:

    Java 21+

    Node.js 18+

    PostgreSQL rodando localmente

    Conta Gmail com App Password (para envio de e-mails)


--- 

Autor: Ronaldo Luiz 
