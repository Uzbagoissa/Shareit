package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {

    /*public static BookingDtoIn toBookingDtoIn(Booking booking) {
        BookingDtoIn bookingDtoIn = new BookingDtoIn();
        bookingDtoIn.setItemId(booking.getId());
        bookingDtoIn.setStart(booking.getStart());
        bookingDtoIn.setEnd(booking.getEnd());
        return bookingDtoIn;
    }*/

    public static BookingDtoOut toBookingDtoOut(Booking booking) {
        BookingDtoOut bookingDtoOut = new BookingDtoOut();
        bookingDtoOut.setId(booking.getId());
        bookingDtoOut.setStart(booking.getStart());
        bookingDtoOut.setEnd(booking.getEnd());
        bookingDtoOut.setStatus(booking.getStatus());
        return bookingDtoOut;
    }

    public static Booking toBooking(long userId, BookingDtoIn bookingDtoIn) {
        Booking booking = new Booking();
        booking.setStart(bookingDtoIn.getStart());
        booking.setEnd(bookingDtoIn.getEnd());
        booking.setBookerId(userId);
        booking.setItemId(bookingDtoIn.getItemId());
        return booking;
    }

    public static List<BookingDtoOut> toListBookingDtoOut(Iterable<Booking> bookings) {
        List<BookingDtoOut> result = new ArrayList<>();
        for (Booking booking : bookings) {
            result.add(toBookingDtoOut(booking));
        }
        return result;
    }

    /*public static LocalDate dateFormat(LocalDate date) {
        String format = DateTimeFormatter
                .ofPattern("yyyy.MM.dd hh:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(date);
        LocalDate formatDate = LocalDate.parse(format);
        return formatDate;
    }*/
}
