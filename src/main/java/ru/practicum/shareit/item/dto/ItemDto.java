package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.CommentDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    long id;
    @NotNull
    @NotBlank(message = "Ошибка: имя пустое или содержит только пробелы")
    String name;
    @NotNull
    @NotBlank(message = "Ошибка: описание пустое или содержит только пробелы")
    String description;
    Boolean available;
    Optional lastBooking;
    Optional nextBooking;
    List<CommentDto> comments;
    Long requestId;
}
