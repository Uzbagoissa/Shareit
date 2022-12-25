package ru.practicum.shareit.item;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    @NonNull
    @NotBlank(message = "Ошибка: комментарий пустой или содержит только пробелы")
    private String text;
    private String authorName;
    private LocalDateTime created;
}