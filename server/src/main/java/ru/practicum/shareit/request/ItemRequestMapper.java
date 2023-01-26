package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Service
public final class ItemRequestMapper {

    public static ItemRequestDtoOut toItemRequestDtoOut(ItemRequest itemRequest) {
        ItemRequestDtoOut itemRequestDtoOut = new ItemRequestDtoOut();
        itemRequestDtoOut.setId(itemRequest.getId());
        itemRequestDtoOut.setDescription(itemRequest.getDescription());
        itemRequestDtoOut.setCreated(itemRequest.getCreated());
        itemRequestDtoOut.setItems(itemRequest.getItems());
        return itemRequestDtoOut;
    }

    public static ItemRequest toItemRequest(long requesterId, ItemRequestDtoIn itemRequestDtoIn) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDtoIn.getDescription());
        itemRequest.setRequesterId(requesterId);
        return itemRequest;
    }
}
