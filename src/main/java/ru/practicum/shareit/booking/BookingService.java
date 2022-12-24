package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    BookingDtoOut saveBooking(long bookerId, BookingDtoIn bookingDtoIn);

    BookingDtoOut getBookingById(long userId, long id);

    BookingDtoOut updateBooking(long ownerId, String approved, long id);

    List<BookingDtoOut> getAllBookingsByBookerId(long bookerId, String state);

    List<BookingDtoOut> getAllBookingsByOwnerId(long ownerId, String state);

    //List<Booking> getLastBookingByItemId(long itemId);

}
