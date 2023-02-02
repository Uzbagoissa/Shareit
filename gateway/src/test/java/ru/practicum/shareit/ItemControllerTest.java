package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemClient itemClient;
    @Autowired
    private MockMvc mvc;

    @Test
    void saveItem() throws Exception {
        long userId = 2;
        ItemDto itemDto = makeItemDto("камера", "делать видео", true, 2);
        when(itemClient.saveItem(anyLong(), any(ItemDto.class)))
                .thenThrow(new IncorrectParameterException("Нужно указать наличие вещи!"));
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    /*указано отрицательно число номера страницы*/
    @Test
    void getAllItemsWithIncorrectParameterFrom() throws Exception {
        long userId = 1;
        long from = -1;
        when(itemClient.getAllItems(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("from"));
        mvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from)))
                .andExpect(status().isBadRequest());
    }

    /*указано количество вывода на страницу = 0*/
    @Test
    void getAllItemsWithIncorrectParameterSize() throws Exception {
        long userId = 1;
        long size = 0;
        when(itemClient.getAllItems(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("size"));
        mvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
    }

    /*указано отрицательно число номера страницы*/
    @Test
    void searchItemsWithIncorrectParameterFrom() throws Exception {
        long userId = 1;
        long from = -1;
        String text = "Аме";
        when(itemClient.searchItems(anyLong(), anyString(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("Неверный параметр from: {}, from должен быть больше 0 " + from));
        mvc.perform(get("/items/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("text", text))
                .andExpect(status().isBadRequest());
    }

    /*указано количество вывода на страницу = 0*/
    @Test
    void searchItemsWithIncorrectParameterSize() throws Exception {
        long userId = 1;
        long size = 0;
        String text = "Аме";
        when(itemClient.searchItems(anyLong(), anyString(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("Неверный параметр size: {}, size должен быть больше 0 " + size));
        mvc.perform(get("/items/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("size", String.valueOf(size))
                        .param("text", text))
                .andExpect(status().isBadRequest());
    }

    private static ItemDto makeItemDto(String name, String description, Boolean available, long requestId) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        return itemDto;
    }
}
