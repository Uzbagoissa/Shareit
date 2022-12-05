package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItems(long userId);
    Item getItemById(long id);
    ItemDto saveItem(long userId, ItemDto itemDto);
    ItemDto updateItem(long userId, ItemDto itemDto, long id);
}