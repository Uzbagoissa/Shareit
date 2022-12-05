package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public List<ItemDto> getAllItems(long userId) {
        return items.values().stream()
                .filter(item -> (item.getUserId()==userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(long id) {
        return null;
    }

    @Override
    public ItemDto saveItem(Item item, ItemDto itemDto) {
        item.setId(getId());
        itemDto.setId(getId());
        items.put(item.getId(), item);
        return itemDto;
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long id) {
        return itemDto;
    }

    private long getId() {
        long lastId = items.values().stream()
                .mapToLong(Item::getId)
                .max()
                .orElse(0);
        return lastId + 1;
    }
}
