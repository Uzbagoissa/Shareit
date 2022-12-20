package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Получили всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id") long id) {
        log.info("Получили пользователя c id: {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDto saveUser(@Valid @RequestBody UserDto userDto) {
        log.info("Добавили нового пользователя");
        return userService.saveUser(userDto);
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable("id") long id) {
        log.info("Удалили пользователя");
        userService.removeUser(id);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@RequestBody UserDto userDto,
                              @PathVariable("id") long id) {
        log.info("Обновили пользователя c id: {}", id);
        return userService.updateUser(userDto, id);
    }
}
