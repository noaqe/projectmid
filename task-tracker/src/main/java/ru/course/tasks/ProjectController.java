package ru.course.tasks;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectController {
    private final ProjectRepository projects;

    public ProjectController(ProjectRepository projects) {
        this.projects = projects;
    }

    @GetMapping({"/projects", "/projects/"})
    public List<Project> list() {
        return projects.list();
    }

    @GetMapping("/projects/{id}")
    public Project get(@PathVariable long id) {
        return projects.get(id);
    }

    @PostMapping({"/projects", "/projects/"})
    public ResponseEntity<Project> create(@Valid @RequestBody Project.Input input) {
        Project project = projects.create(input);
        return ResponseEntity.created(URI.create("/api/projects/" + project.id())).body(project);
    }

    @PutMapping("/projects/{id}")
    public Project update(@PathVariable long id, @Valid @RequestBody Project.Input input) {
        return projects.update(id, input);
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        projects.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports/projects")
    public List<ProjectSummary> report() {
        return projects.report();
    }
}
