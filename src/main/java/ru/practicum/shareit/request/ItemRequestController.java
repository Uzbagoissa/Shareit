package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDtoOut saveItemRequest(@RequestHeader("X-Sharer-User-Id") long requesterId,
                                             @Valid @RequestBody ItemRequestDtoIn itemRequestDtoIn) {
        log.info("Добавили новый запрос");
        return itemRequestService.saveItemRequest(requesterId, itemRequestDtoIn);
    }

    @GetMapping
    public List<ItemRequestDtoOut> getAllItemRequestByRequesterId(@RequestHeader("X-Sharer-User-Id") long requesterId) {
        log.info("Получили все запросы пользователя с id {}", requesterId);
        return itemRequestService.getAllItemRequestByRequesterId(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoOut> getAllItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                     @RequestParam(value = "from", defaultValue = "0") long from,
                                                     @RequestParam(value = "size", defaultValue = "1") long size) {
        if (from < 0) {
            log.info("Неверный параметр from: {}, from должен быть больше 0 ", from);
            throw new IncorrectParameterException("from");
        }
        if (size <= 0) {
            log.info("Неверный параметр size: {}, size должен быть больше 0 ", size);
            throw new IncorrectParameterException("size");
        }
        log.info("Получили все запросы других пользователей");
        return itemRequestService.getAllItemRequest(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoOut getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @PathVariable("requestId") Long requestId) {
        log.info("Получили запрос с id {}", requestId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}
