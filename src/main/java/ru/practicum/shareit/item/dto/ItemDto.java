package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private long id;
    @NotNull
    @NotBlank(message = "Ошибка: имя пустое или содержит только пробелы")
    private String name;
    @NotNull
    @NotBlank(message = "Ошибка: описание пустое или содержит только пробелы")
    private String description;
    private Boolean available;
    private Booking lastBooking;
    private Booking nextBooking;
}
