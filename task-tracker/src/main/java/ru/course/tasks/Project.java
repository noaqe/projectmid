package ru.course.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Project(long id, String name) {
    public record Input(
            @NotBlank(message = "Укажите название проекта.")
            @Size(max = 100, message = "Максимум 100 символов.")
            String name
    ) {}
}
