package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items")
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(value = "from", defaultValue = "0") long from,
                                              @RequestParam(value = "size", defaultValue = "10") long size) {
        if (from < 0) {
            log.info("Неверный параметр from: {}, from должен быть больше 0 ", from);
            throw new IncorrectParameterException("Неверный параметр from: {}, from должен быть больше 0 " + from);
        }
        if (size <= 0) {
            log.info("Неверный параметр size: {}, size должен быть больше 0 ", size);
            throw new IncorrectParameterException("Неверный параметр size: {}, size должен быть больше 0 " + size);
        }
        log.info("Получили все вещи");
        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                               @PathVariable("id") long id) {
        log.info("Получили вещь id {}", id);
        return itemClient.getItemById(userId, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestParam(value = "text") String text,
                                     @RequestParam(value = "from", defaultValue = "0") long from,
                                     @RequestParam(value = "size", defaultValue = "10") long size) {
        if (from < 0) {
            log.info("Неверный параметр from: {}, from должен быть больше 0 ", from);
            throw new IncorrectParameterException("Неверный параметр from: {}, from должен быть больше 0 " + from);
        }
        if (size <= 0) {
            log.info("Неверный параметр size: {}, size должен быть больше 0 ", size);
            throw new IncorrectParameterException("Неверный параметр size: {}, size должен быть больше 0 " + size);
        }
        log.info("Нашли указанные вещи");
        return itemClient.searchItems(userId, text, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> saveItem(@RequestHeader("X-Sharer-User-Id") long userId,
                            @Valid @RequestBody ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            log.error("Нужно указать наличие вещи!");
            throw new IncorrectParameterException("Нужно указать наличие вещи!");
        }
        log.info("Добавили новую вещь");
        return itemClient.saveItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> saveComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody CommentDto commentDto,
                                  @PathVariable("itemId") long itemId) {
        log.info("Добавили новый комментарий");
        return itemClient.saveComment(userId, commentDto, itemId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable("id") long id) {
        log.info("Обновили вещь c id: {}", id);
        return itemClient.updateItem(userId, itemDto, id);
    }
}
