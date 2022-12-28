package ru.practicum.shareit.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class UserDto {
    long id;
    @NotNull
    @NotBlank(message = "Ошибка: имя пустое или содержит только пробелы")
    String name;
    @NotNull
    @NotBlank(message = "Ошибка: email пустой или содержит только пробелы")
    @Email(message = "Ошибка в записи email")
    String email;
}