package ru.course.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record Task(long id, String title, String description, long projectId, String projectName,
                   Long assigneeId, String assigneeName, String statusCode, String statusName, LocalDate dueDate) {
    public record Input(
            @NotBlank(message = "Укажите название задачи.")
            @Size(max = 160, message = "Максимум 160 символов.")
            String title,

            @Size(max = 1000, message = "Максимум 1000 символов.")
            String description,

            @NotNull(message = "Выберите проект.")
            @Positive(message = "ID проекта должен быть положительным.")
            Long projectId,

            @Positive(message = "ID исполнителя должен быть положительным.")
            Long assigneeId,

            @NotBlank(message = "Выберите статус.")
            @Size(max = 20, message = "Максимум 20 символов.")
            String statusCode,

            LocalDate dueDate
    ) {}
}
