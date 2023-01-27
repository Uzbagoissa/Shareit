package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Validated
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Получили всех пользователей");
        return userClient.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable("id") long id) {
        log.info("Получили пользователя c id: {}", id);
        return userClient.getUserById(id);
    }

    @PostMapping
    public ResponseEntity<Object> saveUser(@Valid @RequestBody UserDto userDto) {
        log.info("Добавили нового пользователя");
        return userClient.saveUser(userDto);
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable("id") long id) {
        log.info("Удалили пользователя");
        userClient.removeUser(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@RequestBody UserDto userDto,
                                             @PathVariable("id") long id) {
        log.info("Обновили пользователя c id: {}", id);
        return userClient.updateUser(userDto, id);
    }
}
