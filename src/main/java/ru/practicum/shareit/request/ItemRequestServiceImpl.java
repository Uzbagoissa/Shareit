package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemRequestServiceImpl implements ItemRequestService {
    ItemRequestRepository repository;
    UserService userService;
    ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDtoOut saveItemRequest(long requesterId, ItemRequestDtoIn itemRequestDtoIn) {
        userValid(requesterId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requesterId, itemRequestDtoIn);
        itemRequest.setCreated(LocalDateTime.now());
        repository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDtoOut(itemRequest);
    }

    @Override
    public List<ItemRequestDtoOut> getAllItemRequestByRequesterId(long requesterId) {
        userValid(requesterId);
        return addItemsToItemRequests().stream()
                .filter(a -> a.getRequesterId().equals(requesterId))
                .sorted(Comparator.comparing(ItemRequest::getCreated))
                .map(ItemRequestMapper::toItemRequestDtoOut)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDtoOut> getAllItemRequest(long userId, long from, long size) {
        return addItemsToItemRequests().stream()
                .filter(a -> !a.getRequesterId().equals(userId))
                .skip(from)
                .limit(size)
                .sorted(Comparator.comparing(ItemRequest::getCreated))
                .map(ItemRequestMapper::toItemRequestDtoOut)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoOut getItemRequestById(long userId, Long requestId) {
        userValid(userId);
        itemRequestValid(requestId);
        ItemRequest itemRequest = repository.getById(requestId);
        itemRequest.setItems(addItemToList(requestId));
        return ItemRequestMapper.toItemRequestDtoOut(itemRequest);
    }

    private List<ItemRequest> addItemsToItemRequests() {
        List<ItemRequest> itemRequests = new ArrayList<>();
        for (ItemRequest itemRequest : repository.findAll()) {
            itemRequest.setItems(addItemToList(itemRequest.getId()));
            itemRequests.add(itemRequest);
        }
        return itemRequests;
    }

    private List<Item> addItemToList(Long requestId) {
        List<Item> items = new ArrayList<>();
        for (Item item : itemRepository.findAll()) {
            if (requestId.equals(item.getRequestId())) {
                items.add(item);
            }
        }
        return items;
    }

    private void userValid(long userId) {
        if (!userService.getAllUsers().stream()
                .map(UserDto::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("Пользователя с id не существует! {}", userId);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
    }

    private void itemRequestValid(long id) {
        if (!repository.findAll().stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList())
                .contains(id)) {
            log.error("Запроса с id не существует! {}", id);
            throw new NotFoundException("Запроса с таким id не существует!");
        }
    }
}
