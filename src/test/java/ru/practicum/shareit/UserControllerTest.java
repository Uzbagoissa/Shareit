package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        userDtos = List.of(new UserDto(1, "Иван", "ivan@mail.ru"),
                new UserDto(2, "Петр", "petr@mail.ru"));
    }

    @Test
    void saveUser() throws Exception {
        UserDto userDto = makeUserDto("ivanko@mail.ru", "Иваныч");
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
        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("Иван")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email", Matchers.is("ivan@mail.ru")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is("Петр")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].email", Matchers.is("petr@mail.ru")));
    }

    @Test
    void getUserById() throws Exception {
        when(userService.getUserById(1))
                .thenReturn(userDtos.get(0));
        mvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("name", Matchers.is("Иван")))
                .andExpect(MockMvcResultMatchers.jsonPath("email", Matchers.is("ivan@mail.ru")));
    }

    @Test
    void updateUser() throws Exception {
        UserDto userDto = makeUserDto("ivanko@mail.ru", "Иваныч");
        when(userService.updateUser(userDto, 1))
                .thenReturn(userDto);
        mvc.perform(patch("/users/1")
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
    void removeUser() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
        verify(userService).removeUser(1);
    }

    @Test
    void getNotExistUserById() throws Exception {
        when(userService.getUserById(100))
                .thenThrow(new NotFoundException("Пользователя с таким id не существует!"));
        mvc.perform(get("/users/100")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveUserWithIncorrectFields() throws Exception {
        UserDto userDto = makeUserDto("ivanko@mail.ru", null);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        UserDto userDto1 = makeUserDto(null, "Иваныч");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        UserDto userDto2 = makeUserDto("incorrectmail", "Иваныч");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private UserDto makeUserDto(String email, String name) {
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setName(name);
        return userDto;
    }
}