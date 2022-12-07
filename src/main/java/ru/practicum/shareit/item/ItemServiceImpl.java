package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

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
        userValid(userId);
        return repository.getAllItems(userId);
    }

    @Override
    public ItemDto getItemById(long userId, long id) {
        userValid(userId);
        return repository.getItemById(userId, id);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return repository.searchItems(text);
    }

    @Override
    public ItemDto saveItem(long userId, ItemDto itemDto) {
        userValid(userId);
        return repository.saveItem(userId, itemDto);
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto, long id) {
        userValid(userId);
        return repository.updateItem(userId, itemDto, id);
    }

    private void userValid (long userId){
        if (!userService.getAllUsers().stream()
                .map(UserDto::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("Пользователя с таким id не существует! {}", userId);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
    }
}