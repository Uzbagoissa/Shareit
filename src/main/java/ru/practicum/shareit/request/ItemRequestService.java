package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;

import javax.validation.Valid;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoOut saveItemRequest(long requesterId, ItemRequestDtoIn itemRequestDtoIn);
    List<ItemRequestDtoOut> getAllItemRequestByRequesterId(long requesterId);
    List<ItemRequestDtoOut> getAllItemRequest(long userId, long from, long size);
    ItemRequestDtoOut getItemRequestById(long userId, Long requestId);
}
