package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.user.dto.UserDto;

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
        log.info("Получили запрос на бронирование с id {}", id);
        return bookingService.getBookingById(id);
    }

    @PostMapping
    public BookingDtoOut saveBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @Valid @RequestBody BookingDtoIn bookingDtoIn) {
        log.info("Добавили новый запрос на бронирование");
        return bookingService.saveBooking(userId, bookingDtoIn);
    }

}
