package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingDtoOut saveBooking(long bookerId, BookingDtoIn bookingDtoIn);

    BookingDtoOut getBookingById(long id);

    BookingDtoOut updateBooking(long ownerId, String approved, long id);

    List<BookingDtoOut> getAllBookingsByBookerId(long bookerId, String state);

    List<BookingDtoOut> getAllBookingsByOwnerId(long ownerId, String state);
}
