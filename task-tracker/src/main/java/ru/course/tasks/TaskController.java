package ru.course.tasks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskRepository tasks;

    public TaskController(TaskRepository tasks) {
        this.tasks = tasks;
    }

    @GetMapping({"", "/"})
    public List<Task> list(@RequestParam(defaultValue = "") @Size(max = 160) String q) {
        return tasks.search(q);
    }

    @GetMapping("/{id}")
    public Task get(@PathVariable long id) {
        return tasks.get(id);
    }

    @PostMapping({"", "/"})
    public ResponseEntity<Task> create(@Valid @RequestBody Task.Input input) {
        Task task = tasks.create(input);
        return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable long id, @Valid @RequestBody Task.Input input) {
        return tasks.update(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        tasks.delete(id);
        return ResponseEntity.noContent().build();
    }
}
