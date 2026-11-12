# Учёт задач

Курсовой проект на Java 21, Spring Boot 3.5 и PostgreSQL 18. Каждая задача относится к проекту и имеет статус. Исполнителя и срок можно назначить позже. Браузер показывает ответы API в формате JSON.

## Запуск через Docker

Распакуйте ZIP, откройте терминал в папке `task-tracker` рядом с `compose.yaml` и выполните команду. На компьютере или сервере должен работать Docker Engine с Docker Compose v2. Для первой сборки нужен интернет.

```sh
docker compose up --build -d
```

Когда приложение запустится, откройте [список задач](http://localhost:8080/api/tasks/). На чистой базе это массив из **6 задач**. Стартовые записи создаются автоматически. Состояние контейнеров показывает `docker compose ps`, а запуск приложения удобно проверить командой `docker compose logs -f app`.

| Что открыть в браузере | Что должно быть на старте |
| --- | --- |
| [Все задачи](http://localhost:8080/api/tasks/) | 6 записей с названиями проектов и статусов |
| [Поиск по слову SQL](http://localhost:8080/api/tasks?q=sql) | «Подготовить схему БД» и «Написать примеры JOIN». Слово SQL встречается в описаниях |
| [Отчёт по проектам](http://localhost:8080/api/reports/projects) | У «Сайта кафедры» и «Практикума по SQL» по 3 задачи, по 1 завершённой. У «Архива работ» оба значения равны 0 |

Для запуска на сервере скопируйте туда проект и выполните ту же команду. С другого компьютера откройте `http://АДРЕС_СЕРВЕРА:8080/api/tasks/`. Для учебного показа API доступен без авторизации. Порт базы данных используется только внутри сети Compose. До **первого запуска на сервере** задайте собственный пароль БД в файле `.env` рядом с `compose.yaml` строкой `DB_PASSWORD=ваш_пароль`. Учебный запуск на своём компьютере работает с настройками по умолчанию.

Если порт 8080 занят, добавьте в `.env` строку `APP_PORT=8081` и используйте 8081 в адресах. Команда `docker compose down` останавливает проект. Именованный том сохраняет задачи. Команда `docker compose up -d` поднимает его снова.

## Как устроен проект

| Файл или каталог | Содержание |
| --- | --- |
| `src/main/java/ru/course/tasks/` | Контроллеры API, репозитории с SQL, модели запросов и ответов |
| `src/main/resources/db/migration/V1__schema.sql` | Четыре предметные таблицы, ключи, ограничения и индекс |
| `src/main/resources/db/migration/V2__demo_data.sql` | 3 проекта, 3 исполнителя, 3 статуса и 6 задач |
| `pom.xml`, `dist/task-tracker.jar` | Описание сборки Maven и готовое приложение с зависимостями |
| `Dockerfile`, `compose.yaml` | Сборка образа Java и запуск приложения с PostgreSQL |
| [Отчёт для защиты](docs/report.pdf) | Две страницы с объяснением 3НФ, диаграммами и примером отчёта |
| [Исходники диаграмм](docs/diagrams.md) | ER и UML-диаграмма классов в формате Mermaid |

Spring MVC принимает HTTP-запрос. Для записи контроллер проверяет входную модель с помощью `@Valid`. Репозиторий передаёт SQL и параметры через `JdbcClient` в PostgreSQL. Результат запроса превращается в Java-объект, а Spring возвращает его как JSON. Создание и обновление задачи выполняются в транзакции вместе с чтением результата. При старте Flyway применяет новые SQL-миграции и записывает их версии в служебную таблицу.

## Методы API

Все пути в таблице начинаются с `/api`. Для POST и PUT используйте `Content-Type: application/json`. PUT полностью задаёт новые значения полей ресурса.

| Ресурс | Запросы |
| --- | --- |
| Задачи | `GET /tasks/`, `GET /tasks/{id}`, `POST /tasks`, `PUT /tasks/{id}`, `DELETE /tasks/{id}` |
| Проекты | `GET /projects`, `GET /projects/{id}`, `POST /projects`, `PUT /projects/{id}`, `DELETE /projects/{id}` |
| Поиск | `GET /tasks?q=текст` ищет подстроку в названии и описании без учёта регистра |
| Справочники | `GET /assignees` для списка исполнителей, `GET /statuses` для кодов статусов |
| Отчёт | `GET /reports/projects` для числа всех и завершённых задач в каждом проекте |

Тело проекта содержит поле `name` длиной от 1 до 100 символов. Название должно быть уникальным. Для задачи обязательны непустое `title` до 160 символов, существующий `projectId` и `statusCode` из справочника. Значения статуса на старте — `TODO`, `IN_PROGRESS`, `DONE`. Необязательные поля — `description` до 1000 символов, `assigneeId` и `dueDate` в формате `YYYY-MM-DD`.

**Пример тела запроса для создания задачи в первом проекте**

```json
{
  "title": "Подготовить презентацию",
  "description": "Добавить схему данных на второй слайд",
  "projectId": 1,
  "assigneeId": 2,
  "statusCode": "TODO",
  "dueDate": "2026-11-25"
}
```

При создании API возвращает `201`, заголовок `Location` и объект с новым ID. Чтение и изменение возвращают `200`, удаление — `204`. Ошибки полей, JSON и ссылок дают `400`, запрос отсутствующей записи — `404`. Повторное название проекта и удаление проекта с задачами дают `409`. Подробности доступны в поле `detail` JSON-ответа, ошибки отдельных полей — в `fields`.

## Демонстрация CRUD в PowerShell

Выполняйте строки по порядку в одном окне. Сценарий создаёт свой проект и задачу, меняет их, показывает результат и в конце удаляет обе созданные записи. Стартовые задачи остаются на месте.

```powershell
$api = 'http://localhost:8080/api'
$json = 'application/json; charset=utf-8'

$p = Invoke-RestMethod -Method Post -Uri "$api/projects" -ContentType $json -Body '{"name":"Защита курсовой"}'
Invoke-RestMethod "$api/projects/$($p.id)"
Invoke-RestMethod -Method Put -Uri "$api/projects/$($p.id)" -ContentType $json -Body '{"name":"Демонстрация проекта"}'

$body = @{ title = 'Показать отчёт'; projectId = $p.id; statusCode = 'TODO' }
$t = Invoke-RestMethod -Method Post -Uri "$api/tasks" -ContentType $json -Body ($body | ConvertTo-Json)
Invoke-RestMethod "$api/tasks/$($t.id)"
$body.statusCode = 'DONE'
Invoke-RestMethod -Method Put -Uri "$api/tasks/$($t.id)" -ContentType $json -Body ($body | ConvertTo-Json)
Invoke-RestMethod "$api/reports/projects" | Format-Table

Invoke-RestMethod -Method Delete -Uri "$api/tasks/$($t.id)"
Invoke-RestMethod -Method Delete -Uri "$api/projects/$($p.id)"
```

## Запуск готового JAR без Docker

Потребуются Java 21 и PostgreSQL 18. В `psql` под администратором создайте роль, задайте ей пароль по приглашению и создайте пустую базу. Эти команды выполняются один раз.

```sql
CREATE ROLE taskapp LOGIN;
\password taskapp
CREATE DATABASE tasktracker OWNER taskapp;
```

В PowerShell из папки проекта укажите тот же пароль и запустите собранный JAR. Java должна быть доступна по команде `java` (проверка — `java -version`).

```powershell
$env:DB_PASSWORD = 'ваш_пароль'
java -jar dist/task-tracker.jar
```

По умолчанию приложение подключается к `jdbc:postgresql://localhost:5432/tasktracker` под пользователем `taskapp`. Для другой базы задайте переменные `DB_URL` и `DB_USER`. На Linux пароль задаётся через `export DB_PASSWORD='ваш_пароль'`. Порт приложения задаётся переменной `PORT`. Для самостоятельной сборки установите JDK 21 и Maven 3.9, выполните `mvn clean package` и запустите `java -jar target/task-tracker.jar`.
