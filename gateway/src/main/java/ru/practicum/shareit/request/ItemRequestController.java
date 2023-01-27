package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> saveItemRequest(@RequestHeader("X-Sharer-User-Id") long requesterId,
                                                  @Valid @RequestBody ItemRequestDtoIn itemRequestDtoIn) {
        log.info("Добавили новый запрос");
        return itemRequestClient.saveItemRequest(requesterId, itemRequestDtoIn);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemRequestByRequesterId(@RequestHeader("X-Sharer-User-Id") long requesterId) {
        log.info("Получили все запросы пользователя с id {}", requesterId);
        return itemRequestClient.getAllItemRequestByRequesterId(requesterId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                    @RequestParam(value = "from", defaultValue = "0") long from,
                                                    @RequestParam(value = "size", defaultValue = "1") long size) {
        if (from < 0) {
            log.info("Неверный параметр from: {}, from должен быть больше 0 ", from);
            throw new IncorrectParameterException("Неверный параметр from: {}, from должен быть больше 0 " + from);
        }
        if (size <= 0) {
            log.info("Неверный параметр size: {}, size должен быть больше 0 ", size);
            throw new IncorrectParameterException("Неверный параметр size: {}, size должен быть больше 0 " + size);
        }
        log.info("Получили все запросы других пользователей");
        return itemRequestClient.getAllItemRequest(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                     @PathVariable("requestId") Long requestId) {
        log.info("Получили запрос с id {}", requestId);
        return itemRequestClient.getItemRequestById(userId, requestId);
    }
}
