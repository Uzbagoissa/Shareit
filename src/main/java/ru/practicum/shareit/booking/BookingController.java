package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.exceptions.IncorrectParameterException;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{id}")
    public BookingDtoOut getBookingById(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @PathVariable("id") long id) {
        log.info("Получили бронирование с id {}", id);
        return bookingService.getBookingById(userId, id);
    }

    @GetMapping
    public List<BookingDtoOut> getAllBookingsByBookerId(@RequestHeader("X-Sharer-User-Id") long bookerId,
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
        return bookingService.getAllBookingsByBookerId(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getAllBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") long ownerId,
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
        return bookingService.getAllBookingsByOwnerId(ownerId, state, from, size);
    }

    @PostMapping
    public BookingDtoOut saveBooking(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                     @Valid @RequestBody BookingDtoIn bookingDtoIn) {
        log.info("Добавили новый запрос на бронирование");
        return bookingService.saveBooking(bookerId, bookingDtoIn);
    }

    @PatchMapping("/{id}")
    public BookingDtoOut updateBooking(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                       @RequestParam(value = "approved") String approved,
                                       @PathVariable("id") long id) {
        log.info("Обновили статус запроса c id: {}", id);
        return bookingService.updateBooking(ownerId, approved, id);
    }

}
