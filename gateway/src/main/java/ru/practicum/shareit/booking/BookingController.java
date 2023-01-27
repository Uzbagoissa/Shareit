package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.exceptions.IncorrectParameterException;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable("id") long id) {
        log.info("Получили бронирование с id {}", id);
        return bookingClient.getBookingById(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsByBookerId(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                           @RequestParam(value = "state", defaultValue = "ALL") String state,
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
        log.info("Получили все бронирования пользователя с id {}", bookerId);
        return bookingClient.getAllBookingsByBookerId(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                          @RequestParam(value = "state", defaultValue = "ALL") String state,
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
        log.info("Получили все забронированные вещи пользователя с id {}", ownerId);
        return bookingClient.getAllBookingsByOwnerId(ownerId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> saveBooking(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                              @Valid @RequestBody BookingDtoIn bookingDtoIn) {
        if (bookingDtoIn.getEnd().isBefore(LocalDateTime.now())) {
            log.error("Некорректное время окончания аренды!");
            throw new IncorrectParameterException("Некорректное время окончания аренды!");
        }
        if (bookingDtoIn.getEnd().isBefore(bookingDtoIn.getStart())) {
            log.error("Время окончания аренды раньше времени начала аренды!");
            throw new IncorrectParameterException("Время окончания аренды раньше времени начала аренды!");
        }
        if (bookingDtoIn.getStart().isBefore(LocalDateTime.now())) {
            log.error("Некорректное время начала аренды!");
            throw new IncorrectParameterException("Некорректное время начала аренды!");
        }
        log.info("Добавили новый запрос на бронирование");
        return bookingClient.saveBooking(bookerId, bookingDtoIn);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateBooking(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                @RequestParam(value = "approved") String approved,
                                                @PathVariable("id") long id) {
        log.info("Обновили статус запроса c id: {}", id);
        return bookingClient.updateBooking(ownerId, approved, id);
    }
}
