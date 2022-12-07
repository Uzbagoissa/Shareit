package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final List<String> searchHistory = new ArrayList<>();
    private final ItemMapper itemMapper = new ItemMapper();

    @Override
    public List<ItemDto> getAllItems(long userId) {
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getUserId() == userId) {
                itemDtos.add(itemMapper.toItemDto(item));
            }
        }
        return itemDtos;
    }

    @Override
    public ItemDto getItemById(long userId, long id) {
        itemValid (id);
        return itemMapper.toItemDto(items.get(id));
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        List<ItemDto> itemDtosSearched = new ArrayList<>();
        if (text.equals("")) {
            for (Item item : items.values()) {
                if (item.getName().toLowerCase().contains(searchHistory.get(searchHistory.size() - 1).toLowerCase()) && item.getAvailable().equals(true) ||
                        item.getDescription().toLowerCase().contains(searchHistory.get(searchHistory.size() - 1).toLowerCase()) && item.getAvailable().equals(true)) {
                    return itemDtosSearched;
                }
            }
        } else {
            for (Item item : items.values()) {
                if (item.getName().toLowerCase().contains(text.toLowerCase()) && item.getAvailable().equals(true) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()) && item.getAvailable().equals(true)) {
                    itemDtosSearched.add(itemMapper.toItemDto(item));
                    searchHistory.add(text);
                }
            }
        }
        return itemDtosSearched;
    }

    @Override
    public ItemDto saveItem(long userId, ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            log.error("Нужно указать наличие вещи!");
            throw new IncorrectParameterException("Нужно указать наличие вещи!");
        }
        Item item = itemMapper.toItem(itemDto, userId);
        item.setId(getIdforItem());
        itemDto.setId(getIdforItem());
        items.put(item.getId(), item);
        return itemDto;
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto, long id) {
        itemValid (id);
        if (items.get(id).getUserId() != userId) {
            log.error("Пользователь с id {} не может изменять эту вещь с id {}!", userId, id);
            throw new ForbiddenException("Пользователю запрещено изменять чужую вещь!");
        }
        Item item = itemMapper.toItem(itemDto, userId);
        item.setId(id);
        if (itemDto.getName() == null) {
            item.setName(items.get(id).getName());
            itemDto.setName(items.get(id).getName());
        }
        if (itemDto.getDescription() == null) {
            item.setDescription(items.get(id).getDescription());
            itemDto.setDescription(items.get(id).getDescription());
        }
        if (itemDto.getAvailable() == null) {
            item.setAvailable(items.get(id).getAvailable());
            itemDto.setAvailable(items.get(id).getAvailable());
        }
        itemDto.setId(id);
        items.put(item.getId(), item);
        return itemDto;
    }

    private long getIdforItem() {
        long lastId = items.values().stream()
                .mapToLong(Item::getId)
                .max()
                .orElse(0);
        return lastId + 1;
    }

    private void itemValid (long id){
        if (!items.containsKey(id)) {
            log.error("Вещи с таким id не существует! {}", id);
            throw new NotFoundException("Вещи с таким id не существует!");
        }
    }
}
