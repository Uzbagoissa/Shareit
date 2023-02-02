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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    private List<ItemRequestDtoOut> itemRequestDtoOuts;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void addDate() {
        ItemDto item1 = new ItemDto();
        item1.setName("метла");
        ItemDto item2 = new ItemDto();
        item2.setName("дрель");
        ItemDto item3 = new ItemDto();
        item3.setName("кофемашина");

        itemRequestDtoOuts = List.of(new ItemRequestDtoOut(1L, "нужна штука, чтобы делать чисто",
                        LocalDateTime.of(2023, 1, 8, 12, 30, 54), List.of(item1)),
                new ItemRequestDtoOut(2L, "нужна штука, чтобы сверлить",
                        LocalDateTime.of(2023, 1, 12, 12, 30, 54), List.of(item2)));
    }

    @Test
    void saveItemRequest() throws Exception {
        long requesterId = 2;
        ItemRequestDtoOut itemRequestDtoOut = makeItemRequestOut(4L, "нужна штука, чтобы делать видео",
                LocalDateTime.of(2023, 1, 8, 12, 30, 54));
        when(itemRequestService.saveItemRequest(anyLong(), any(ItemRequestDtoIn.class)))
                .thenReturn(itemRequestDtoOut);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requesterId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(itemRequestDtoOut.getId()), long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description",
                        Matchers.is("нужна штука, чтобы делать видео")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created",
                        Matchers.is(LocalDateTime.of(2023, 1, 8, 12, 30, 54).toString())));
    }

    @Test
    void getAllItemRequestByRequesterId() throws Exception {
        long requesterId = 1;
        when(itemRequestService.getAllItemRequestByRequesterId(anyLong()))
                .thenReturn(itemRequestDtoOuts);
        mvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requesterId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description",
                        Matchers.is(itemRequestDtoOuts.get(0).getDescription())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].created",
                        Matchers.is(itemRequestDtoOuts.get(0).getCreated().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description",
                        Matchers.is(itemRequestDtoOuts.get(1).getDescription())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].created",
                        Matchers.is(itemRequestDtoOuts.get(1).getCreated().toString())));
    }

    @Test
    void getAllItemRequest() throws Exception {
        long userId = 1;
        when(itemRequestService.getAllItemRequest(anyLong(), anyLong(), anyLong()))
                .thenReturn(itemRequestDtoOuts);
        mvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description",
                        Matchers.is(itemRequestDtoOuts.get(0).getDescription())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].created",
                        Matchers.is(itemRequestDtoOuts.get(0).getCreated().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description",
                        Matchers.is(itemRequestDtoOuts.get(1).getDescription())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].created",
                        Matchers.is(itemRequestDtoOuts.get(1).getCreated().toString())));
    }

    @Test
    void getItemRequestById() throws Exception {
        long userId = 1;
        when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestDtoOuts.get(1));
        mvc.perform(get("/requests/2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description",
                        Matchers.is(itemRequestDtoOuts.get(1).getDescription())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created",
                        Matchers.is(itemRequestDtoOuts.get(1).getCreated().toString())));
    }

    private ItemRequestDtoOut makeItemRequestOut(Long id, String description, LocalDateTime created) {
        ItemRequestDtoOut itemRequestDtoOut = new ItemRequestDtoOut();
        itemRequestDtoOut.setId(id);
        itemRequestDtoOut.setDescription(description);
        itemRequestDtoOut.setCreated(created);
        return itemRequestDtoOut;
    }
}
