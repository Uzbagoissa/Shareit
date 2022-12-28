package ru.practicum.shareit.item;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    @NonNull
    @NotBlank(message = "Ошибка: комментарий пустой или содержит только пробелы")
    String text;
    String authorName;
    LocalDateTime created;
}