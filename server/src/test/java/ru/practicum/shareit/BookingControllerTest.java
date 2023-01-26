package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private List<BookingDtoOut> bookingDtos;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;
    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void addDate() {
        bookingDtos = List.of(new BookingDtoOut(1L, BookingStatus.APPROVED,
                        LocalDateTime.of(2022, 1, 8, 12, 30, 54),
                        LocalDateTime.of(2022, 3, 8, 12, 30, 54),
                        makeUserDto(1), makeItemDto(1)),
                new BookingDtoOut(2L, BookingStatus.WAITING,
                        LocalDateTime.of(2023, 3, 1, 12, 30, 54),
                        LocalDateTime.of(2023, 3, 8, 12, 30, 54),
                        makeUserDto(2), makeItemDto(1)),
                new BookingDtoOut(3L, BookingStatus.APPROVED,
                        LocalDateTime.of(2022, 3, 8, 12, 30, 54),
                        LocalDateTime.of(2022, 7, 8, 12, 30, 54),
                        makeUserDto(1), makeItemDto(3)));
    }

    @Test
    void saveBooking() throws Exception {
        long bookerId = 2;
        long itemId = 1;
        BookingDtoOut bookingDtoOut = new BookingDtoOut(1L, BookingStatus.WAITING,
                LocalDateTime.of(2023, 2, 8, 12, 30, 54),
                LocalDateTime.of(2023, 3, 8, 12, 30, 54),
                makeUserDto(bookerId), makeItemDto(itemId));
        when(bookingService.saveBooking(anyLong(), any(BookingDtoIn.class)))
                .thenReturn(bookingDtoOut);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookerId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(bookingDtoOut.getId()), long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.start",
                        Matchers.is(LocalDateTime.of(2023, 2, 8, 12, 30, 54).toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.end",
                        Matchers.is(LocalDateTime.of(2023, 3, 8, 12, 30, 54).toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(BookingStatus.WAITING.toString())));
    }

    @Test
    void updateBooking() throws Exception {
        long bookerId = 2;
        long itemId = 1;
        long ownerId = 2;
        String approved = "true";
        BookingDtoOut bookingDtoOut = new BookingDtoOut(1L, BookingStatus.APPROVED,
                LocalDateTime.of(2023, 2, 8, 12, 30, 54),
                LocalDateTime.of(2023, 3, 8, 12, 30, 54),
                makeUserDto(bookerId), makeItemDto(itemId));
        when(bookingService.updateBooking(anyLong(), anyString(), anyLong()))
                .thenReturn(bookingDtoOut);
        mvc.perform(patch("/bookings/1")
                        .content(mapper.writeValueAsString(bookingDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", approved))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(bookingDtoOut.getId()), long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void getBookingById() throws Exception {
        long userId = 1;
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingDtos.get(0));
        mvc.perform(get("/bookings/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(bookingDtos.get(0).getId()), long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.start",
                        Matchers.is(LocalDateTime.of(2022, 1, 8, 12, 30, 54).toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.end",
                        Matchers.is(LocalDateTime.of(2022, 3, 8, 12, 30, 54).toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void getAllBookingsByBookerId() throws Exception {
        long userId = 1;
        when(bookingService.getAllBookingsByBookerId(anyLong(), anyString(), anyLong(), anyLong()))
                .thenReturn(List.of(bookingDtos.get(0), bookingDtos.get(2)));
        mvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(bookingDtos.get(0).getId()), long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start",
                        Matchers.is(LocalDateTime.of(2022, 1, 8, 12, 30, 54).toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].end",
                        Matchers.is(LocalDateTime.of(2022, 3, 8, 12, 30, 54).toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(bookingDtos.get(2).getId()), long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].start",
                        Matchers.is(LocalDateTime.of(2022, 3, 8, 12, 30, 54).toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].end",
                        Matchers.is(LocalDateTime.of(2022, 7, 8, 12, 30, 54).toString())));
    }

    @Test
    void getAllBookingsByOwnerId() throws Exception {
        long ownerId = 1;
        when(bookingService.getAllBookingsByOwnerId(anyLong(), anyString(), anyLong(), anyLong()))
                .thenReturn(List.of(bookingDtos.get(1)));
        mvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(bookingDtos.get(1).getId()), long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start",
                        Matchers.is(LocalDateTime.of(2023, 3, 1, 12, 30, 54).toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].end",
                        Matchers.is(LocalDateTime.of(2023, 3, 8, 12, 30, 54).toString())));
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
