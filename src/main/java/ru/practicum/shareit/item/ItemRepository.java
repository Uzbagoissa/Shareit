package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository {
    List<ItemDto> getAllItems(long userId);
    Item getItemById(long id);
    ItemDto saveItem(Item item, ItemDto itemDto);
    ItemDto updateItem(ItemDto itemDto, long id);
}