package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItems(long userId);

    ItemDto getItemById(long id);

    List<ItemDto> searchItems(String text);

    ItemDto saveItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto, long id);

}