package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository repository;


    @Override
    public List<ItemDto> getAllItems(long userId) {
        userValid(userId);
        return ItemMapper.toListItemDto(repository.findByUserId(userId));
    }

    @Override
    public ItemDto getItemById(long id) {
        itemValid(id);
        return ItemMapper.toItemDto(repository.getById(id));
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return ItemMapper.toListItemDto(repository.searchItems(text));
    }

    @Transactional
    @Override
    public ItemDto saveItem(long userId, ItemDto itemDto) {
        userValid(userId);
        if (itemDto.getAvailable() == null) {
            log.error("Нужно указать наличие вещи!");
            throw new IncorrectParameterException("Нужно указать наличие вещи!");
        }
        return ItemMapper.toItemDto(repository.save(ItemMapper.toItem(userId, itemDto)));
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto, long id) {
        userValid(userId);
        itemValid(id);
        if (repository.getById(id).getUserId() != userId) {
            log.error("Пользователь с id {} не может изменять эту вещь с id {}!", userId, id);
            throw new ForbiddenException("Пользователю запрещено изменять чужую вещь!");
        }
        Item item = ItemMapper.toItem(userId, itemDto);
        if (itemDto.getName() == null) {
            item.setName(repository.getById(id).getName());
            itemDto.setName(repository.getById(id).getName());
        }
        if (itemDto.getDescription() == null) {
            item.setDescription(repository.getById(id).getDescription());
            itemDto.setDescription(repository.getById(id).getDescription());
        }
        if (itemDto.getAvailable() == null) {
            item.setAvailable(repository.getById(id).getAvailable());
            itemDto.setAvailable(repository.getById(id).getAvailable());
        }
        item.setId(id);
        return ItemMapper.toItemDto(repository.save(item));
    }

    private void userValid(long userId) {
        if (!userService.getAllUsers().stream()
                .map(UserDto::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("Пользователя с таким id не существует! {}", userId);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
    }

    private void itemValid(long id) {
        if (!repository.findAll().stream()
                .map(Item::getId)
                .collect(Collectors.toList())
                .contains(id)) {
            log.error("Вещи с таким id не существует! {}", id);
            throw new NotFoundException("Вещи с таким id не существует!");
        }
    }

}