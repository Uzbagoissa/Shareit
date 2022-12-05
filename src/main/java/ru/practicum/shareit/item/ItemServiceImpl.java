package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository repository;
    @Override
    public List<ItemDto> getAllItems(long userId) {
        return repository.getAllItems(userId);
    }

    @Override
    public Item getItemById(long id) {
        return repository.getItemById(id);
    }

    @Override
    public ItemDto saveItem(long userId, ItemDto itemDto) {
        if (!userService.getAllUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("Пользователь с таким id не существует! {}", userId);
            throw new NotFoundException("Пользователь с таким id не существует!");
        }
        Item item = new Item();
        item.setUserId(userId);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.isAvailable());
        return repository.saveItem(item, itemDto);
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto, long id) {
        if (!userService.getAllUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("Пользователь с таким id не существует! {}", userId);
            throw new NotFoundException("Пользователь с таким id не существует!");
        }
        return repository.updateItem(itemDto, id);
    }
}