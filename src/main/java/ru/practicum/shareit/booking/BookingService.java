package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut saveBooking(long bookerId, BookingDtoIn bookingDtoIn);

    BookingDtoOut getBookingById(long userId, long id);

    BookingDtoOut updateBooking(long ownerId, String approved, long id);

    List<BookingDtoOut> getAllBookingsByBookerId(long bookerId, String state, long from, long size);

    List<BookingDtoOut> getAllBookingsByOwnerId(long ownerId, String state, long from, long size);

}
