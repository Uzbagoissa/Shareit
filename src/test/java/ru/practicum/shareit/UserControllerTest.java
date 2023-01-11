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
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    private List<UserDto> userDtos;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void addDate() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("Иван");
        userDto1.setEmail("ivan@mail.ru");
        UserDto userDto2 = new UserDto();
        userDto2.setName("Петр");
        userDto2.setEmail("petr@mail.ru");
        userDtos = new ArrayList<>();
        userDtos.add(userDto1);
        userDtos.add(userDto2);
        userService.saveUser(userDto1);
        userService.saveUser(userDto2);
    }



    @Test
    void saveUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Иваныч");
        userDto.setEmail("ivanko@mail.ru");
        when(userService.saveUser(userDto))
                .thenReturn(userDto);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(userDto.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("Иваныч")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is("ivanko@mail.ru")));
    }

    @Test
    void getAllUsers() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(userDtos);
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(userDtos.get(0).getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("Иван")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email", Matchers.is("ivan@mail.ru")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(userDtos.get(1).getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is("Петр")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].email", Matchers.is("petr@mail.ru")));
    }

    @Test
    void getUserById() throws Exception {
        when(userService.getUserById(1))
                .thenReturn(userDtos.get(0));
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(userDtos.get(0).getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("Иван")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email", Matchers.is("ivan@mail.ru")));
    }
}
