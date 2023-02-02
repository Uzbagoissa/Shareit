package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingClient bookingClient;
    @Autowired
    private MockMvc mvc;

    @Test
    void saveBooking() throws Exception {
        long bookerId = 2;
        long itemId = 1;
        /*время окончания бронирования указано в прошлом*/
        BookingDtoOut bookingDtoOut1 = new BookingDtoOut(1L, BookingStatus.WAITING,
                LocalDateTime.of(2023, 1, 8, 12, 30, 54),
                LocalDateTime.of(2023, 1, 10, 12, 30, 54),
                makeUserDto(bookerId), makeItemDto(itemId));
        when(bookingClient.saveBooking(anyLong(), any(BookingDtoIn.class)))
                .thenThrow(new IncorrectParameterException("Некорректное время окончания аренды!"));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoOut1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookerId))
                .andExpect(status().isBadRequest());

        /*Время окончания аренды раньше времени начала аренды*/
        BookingDtoOut bookingDtoOut2 = new BookingDtoOut(1L, BookingStatus.WAITING,
                LocalDateTime.of(2025, 5, 20, 12, 30, 54),
                LocalDateTime.of(2025, 5, 18, 12, 30, 54),
                makeUserDto(bookerId), makeItemDto(itemId));
        when(bookingClient.saveBooking(anyLong(), any(BookingDtoIn.class)))
                .thenThrow(new IncorrectParameterException("Время окончания аренды раньше времени начала аренды!"));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoOut2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookerId))
                .andExpect(status().isBadRequest());

        /*время начала бронирования указано в прошлом*/
        BookingDtoOut bookingDtoOut3 = new BookingDtoOut(1L, BookingStatus.WAITING,
                LocalDateTime.of(2025, 5, 20, 12, 30, 54),
                LocalDateTime.of(2025, 5, 18, 12, 30, 54),
                makeUserDto(bookerId), makeItemDto(itemId));
        when(bookingClient.saveBooking(anyLong(), any(BookingDtoIn.class)))
                .thenThrow(new IncorrectParameterException("Некорректное время начала аренды!"));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoOut3))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookerId))
                .andExpect(status().isBadRequest());
    }

    /*указано отрицательно число номера страницы*/
    @Test
    void getAllBookingsByBookerIdWithIncorrectParameterFrom() throws Exception {
        long userId = 1;
        long from = -1;
        when(bookingClient.getAllBookingsByBookerId(anyLong(), anyString(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("Неверный параметр from: {}, from должен быть больше 0 " + from));
        mvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from)))
                .andExpect(status().isBadRequest());
    }

    /*указано количество вывода на страницу = 0*/
    @Test
    void getAllBookingsByBookerIdWithIncorrectParameterSize() throws Exception {
        long userId = 1;
        long size = 0;
        when(bookingClient.getAllBookingsByBookerId(anyLong(), anyString(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("Неверный параметр size: {}, size должен быть больше 0 " + size));
        mvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
    }

    /*указано отрицательно число номера страницы*/
    @Test
    void getAllBookingsByOwnerIdWithIncorrectParameterFrom() throws Exception {
        long userId = 1;
        long from = -1;
        when(bookingClient.getAllBookingsByOwnerId(anyLong(), anyString(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("Неверный параметр from: {}, from должен быть больше 0 " + from));
        mvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from)))
                .andExpect(status().isBadRequest());
    }

    /*указано количество вывода на страницу = 0*/
    @Test
    void getAllBookingsByOwnerIdWithIncorrectParameterSize() throws Exception {
        long userId = 1;
        long size = 0;
        when(bookingClient.getAllBookingsByOwnerId(anyLong(), anyString(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("Неверный параметр size: {}, size должен быть больше 0 " + size));
        mvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
    }

    private UserDto makeUserDto(long id) {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        return userDto;
    }

    private static ItemDto makeItemDto(long id) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        return itemDto;
    }
}
