package ru.course.tasks;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LookupController {
    public record Assignee(long id, String name, String email) {}
    public record Status(String code, String name) {}

    private final JdbcClient db;

    public LookupController(JdbcClient db) {
        this.db = db;
    }

    @GetMapping("/assignees")
    public List<Assignee> assignees() {
        return db.sql("SELECT id, name, email FROM assignees ORDER BY id").query(Assignee.class).list();
    }

    @GetMapping("/statuses")
    public List<Status> statuses() {
        return db.sql("SELECT code, name FROM statuses ORDER BY code").query(Status.class).list();
    }
}
