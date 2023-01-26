package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItems(long userId, long from, long size);

    ItemDto getItemById(long userId, long id);

    List<ItemDto> searchItems(long userId, String text, long from, long size);

    ItemDto saveItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto, long id);

    CommentDto saveComment(long userId, CommentDto commentDto, long itemId);

}