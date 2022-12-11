package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    private long id;
    private long userId;
    @NotNull
    @NotBlank(message = "Ошибка: имя пустое или содержит только пробелы")
    private String name;
    @NotNull
    @NotBlank(message = "Ошибка: описание пустое или содержит только пробелы")
    private String description;
    private Boolean available;
    private String request;
}
