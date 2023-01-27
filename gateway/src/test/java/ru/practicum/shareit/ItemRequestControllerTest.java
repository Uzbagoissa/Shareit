package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemRequestClient itemRequestClient;
    @Autowired
    private MockMvc mvc;

    /*указано отрицательно число номера страницы*/
    @Test
    void getAllItemRequestWithIncorrectParameterFrom() throws Exception {
        long userId = 1;
        long from = -1;
        when(itemRequestClient.getAllItemRequest(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("Неверный параметр from: {}, from должен быть больше 0 " + from));
        mvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from)))
                .andExpect(status().isBadRequest());
    }

    /*указано количество вывода на страницу = 0*/
    @Test
    void getAllItemRequestWithIncorrectParameterSize() throws Exception {
        long userId = 1;
        long size = 0;
        when(itemRequestClient.getAllItemRequest(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IncorrectParameterException("Неверный параметр size: {}, size должен быть больше 0 " + size));
        mvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
    }
}
