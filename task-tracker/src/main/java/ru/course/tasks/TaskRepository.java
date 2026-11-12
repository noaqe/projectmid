package ru.course.tasks;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Types;
import java.util.List;

@Repository
public class TaskRepository {
    private static final String SELECT_TASK = """
            SELECT t.id, t.title, t.description, t.project_id, p.name AS project_name,
                   t.assignee_id, a.name AS assignee_name, t.status_code, s.name AS status_name, t.due_date
            FROM tasks t
            JOIN projects p ON p.id = t.project_id
            JOIN statuses s ON s.code = t.status_code
            LEFT JOIN assignees a ON a.id = t.assignee_id
            """;

    private final JdbcClient db;

    public TaskRepository(JdbcClient db) {
        this.db = db;
    }

    public List<Task> search(String q) {
        return db.sql(SELECT_TASK + """
                WHERE strpos(lower(t.title), lower(:q)) > 0
                   OR strpos(lower(t.description), lower(:q)) > 0
                ORDER BY t.id
                """).param("q", q.strip()).query(Task.class).list();
    }

    public Task get(long id) {
        return db.sql(SELECT_TASK + "WHERE t.id = :id")
                .param("id", id).query(Task.class).optional().orElseThrow(this::notFound);
    }

    @Transactional
    public Task create(Task.Input input) {
        var insert = db.sql("""
                INSERT INTO tasks (title, description, project_id, assignee_id, status_code, due_date)
                VALUES (:title, :description, :projectId, :assigneeId, :statusCode, :dueDate)
                RETURNING id
                """);
        long id = bind(insert, input).query(Long.class).single();
        return get(id);
    }

    @Transactional
    public Task update(long id, Task.Input input) {
        var update = db.sql("""
                UPDATE tasks
                SET title = :title, description = :description, project_id = :projectId,
                    assignee_id = :assigneeId, status_code = :statusCode, due_date = :dueDate
                WHERE id = :id
                """).param("id", id);
        if (bind(update, input).update() == 0) {
            throw notFound();
        }
        return get(id);
    }

    public void delete(long id) {
        if (db.sql("DELETE FROM tasks WHERE id = :id").param("id", id).update() == 0) {
            throw notFound();
        }
    }

    private JdbcClient.StatementSpec bind(JdbcClient.StatementSpec sql, Task.Input input) {
        return sql.param("title", input.title().strip())
                .param("description", input.description() == null ? "" : input.description().strip())
                .param("projectId", input.projectId())
                .param("assigneeId", input.assigneeId(), Types.BIGINT)
                .param("statusCode", input.statusCode().strip())
                .param("dueDate", input.dueDate(), Types.DATE);
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена.");
    }
}
