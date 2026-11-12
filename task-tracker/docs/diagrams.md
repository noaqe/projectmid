# Диаграммы проекта

Обе диаграммы также доступны для просмотра и печати в [отчёте PDF](report.pdf).

## Связи таблиц

```mermaid
erDiagram
    projects ||--o{ tasks : "содержит"
    assignees |o--o{ tasks : "выполняет"
    statuses ||--o{ tasks : "описывает состояние"
    projects {
        bigint id PK
        varchar name UK
    }
    assignees {
        bigint id PK
        varchar name
        varchar email UK
    }
    statuses {
        varchar code PK
        varchar name UK
    }
    tasks {
        bigint id PK
        varchar title
        varchar description
        bigint project_id FK
        bigint assignee_id FK "может быть NULL"
        varchar status_code FK
        date due_date "может быть NULL"
    }
```

Каждая задача принадлежит ровно одному проекту и имеет один статус. Ей можно назначить одного исполнителя. Каждый проект, исполнитель или статус может встречаться у любого числа задач.

## Классы приложения

```mermaid
classDiagram
    class TaskController {
        +list(q)
        +get(id)
        +create(input)
        +update(id, input)
        +delete(id)
    }
    class ProjectController {
        +list()
        +get(id)
        +create(input)
        +update(id, input)
        +delete(id)
        +report()
    }
    class LookupController {
        +assignees()
        +statuses()
    }
    class TaskRepository {
        +search(q)
        +get(id)
        +create(input)
        +update(id, input)
        +delete(id)
    }
    class ProjectRepository {
        +list()
        +get(id)
        +create(input)
        +update(id, input)
        +delete(id)
        +report()
    }
    class Task {
        <<record>>
        long id
        String title
        String statusCode
    }
    class Project {
        <<record>>
        long id
        String name
    }
    class ProjectSummary {
        <<record>>
        long projectId
        String projectName
        long totalTasks
        long doneTasks
    }
    TaskController --> TaskRepository
    ProjectController --> ProjectRepository
    TaskRepository --> JdbcClient
    ProjectRepository --> JdbcClient
    LookupController --> JdbcClient
    TaskRepository ..> Task
    ProjectRepository ..> Project
    ProjectRepository ..> ProjectSummary
```

На схеме показаны основные классы. Вложенные записи `Task.Input` и `Project.Input` задают входные поля. Общий обработчик `ApiErrors` формирует ответы при ошибках.
