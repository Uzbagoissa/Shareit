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
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    private List<ItemDto> itemDtos;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemService itemService;
    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void addDate() {
        itemDtos = List.of(new ItemDto(1, "метла", "штука для приборки", true, null,
                        null, null, 1L),
                new ItemDto(2, "дрель", "чтоб сверлить", true, null,
                        null, null, 3L));
    }

    @Test
    void saveItem() throws Exception {
        long userId = 2;
        ItemDto itemDto = makeItemDto("камера", "делать видео", true, 2);
        when(itemService.saveItem(anyLong(), any(ItemDto.class)))
                .thenReturn(itemDto);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(itemDto.getId()), long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("камера")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is("делать видео")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available", Matchers.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requestId", Matchers.is(2)));
    }

    @Test
    void getAllItems() throws Exception {
        long userId = 1;
        when(itemService.getAllItems(anyLong(), anyLong(), anyLong()))
                .thenReturn(itemDtos);
        mvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("метла")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is("штука для приборки")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].available", Matchers.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].requestId", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is("дрель")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description", Matchers.is("чтоб сверлить")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].available", Matchers.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].requestId", Matchers.is(3)));
    }

    @Test
    void getItemById() throws Exception {
        long userId = 1;
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDtos.get(0));
        mvc.perform(get("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("метла")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is("штука для приборки")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available", Matchers.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requestId", Matchers.is(1)));
    }

    @Test
    void updateItem() throws Exception {
        long userId = 1;
        ItemDto itemDto = makeItemDto("камера", "снимать чет там", true, 2);
        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong()))
                .thenReturn(itemDto);
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("камера")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is("снимать чет там")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available", Matchers.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requestId", Matchers.is(2)));
    }

    /*попытка изменить пользователем чужую вещь*/
    @Test
    void updateItemWithIncorrectUser() throws Exception {
        long userId = 2;
        ItemDto itemDto = makeItemDto("камера", "снимать чет там", true, 2);
        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong()))
                .thenThrow(new ForbiddenException("Пользователю запрещено изменять чужую вещь!"));
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchItems() throws Exception {
        long userId = 1;
        String text = "Аме";
        List<ItemDto> itemDtosFound = new ArrayList<>();
        ItemDto itemDto = makeItemDto("камера", "снимать чет там", true, 2);
        itemDtosFound.add(itemDto);
        when(itemService.searchItems(anyLong(), anyString(), anyLong(), anyLong()))
                .thenReturn(itemDtosFound);
        mvc.perform(get("/items/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("камера")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is("снимать чет там")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].available", Matchers.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].requestId", Matchers.is(2)));
    }

    @Test
    void saveComment() throws Exception {
        long userId = 1;
        CommentDto commentDto = makeCommentDto(1L, "хорошая штуковина!", "Петро");
        when(itemService.saveComment(anyLong(), any(CommentDto.class), anyLong()))
                .thenReturn(commentDto);
        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.text", Matchers.is("хорошая штуковина!")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorName", Matchers.is("Петро")));
    }

    private static ItemDto makeItemDto(String name, String description, Boolean available, long requestId) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        return itemDto;
    }

    private static CommentDto makeCommentDto(Long id, String text, String authorName) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(id);
        commentDto.setText(text);
        commentDto.setAuthorName(authorName);
        commentDto.setCreated(LocalDateTime.now());
        return commentDto;
    }
}
