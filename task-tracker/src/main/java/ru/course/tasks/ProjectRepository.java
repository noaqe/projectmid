package ru.course.tasks;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Repository
public class ProjectRepository {
    private final JdbcClient db;

    public ProjectRepository(JdbcClient db) {
        this.db = db;
    }

    public List<Project> list() {
        return db.sql("SELECT id, name FROM projects ORDER BY id").query(Project.class).list();
    }

    public Project get(long id) {
        return db.sql("SELECT id, name FROM projects WHERE id = :id")
                .param("id", id).query(Project.class).optional().orElseThrow(this::notFound);
    }

    public Project create(Project.Input input) {
        return db.sql("INSERT INTO projects (name) VALUES (:name) RETURNING id, name")
                .param("name", input.name().strip()).query(Project.class).single();
    }

    public Project update(long id, Project.Input input) {
        return db.sql("UPDATE projects SET name = :name WHERE id = :id RETURNING id, name")
                .param("name", input.name().strip()).param("id", id)
                .query(Project.class).optional().orElseThrow(this::notFound);
    }

    public void delete(long id) {
        try {
            if (db.sql("DELETE FROM projects WHERE id = :id").param("id", id).update() == 0) {
                throw notFound();
            }
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "В проекте есть задачи. Сначала перенесите или удалите их.", ex);
        }
    }

    public List<ProjectSummary> report() {
        return db.sql("""
                SELECT p.id AS project_id, p.name AS project_name,
                       COUNT(t.id) AS total_tasks,
                       COUNT(t.id) FILTER (WHERE t.status_code = 'DONE') AS done_tasks
                FROM projects p
                LEFT JOIN tasks t ON t.project_id = p.id
                GROUP BY p.id, p.name
                ORDER BY p.id
                """).query(ProjectSummary.class).list();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден.");
    }
}
