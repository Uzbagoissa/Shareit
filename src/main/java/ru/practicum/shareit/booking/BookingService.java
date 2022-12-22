package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

public interface BookingService {
    BookingDtoOut saveBooking(long userId, BookingDtoIn bookingDtoIn);

    BookingDtoOut getBookingById(long id);
}
