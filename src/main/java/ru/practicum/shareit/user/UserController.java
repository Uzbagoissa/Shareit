package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получили всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") long id){
        log.info("Получили пользователя c id: {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public User saveUser(@Valid @RequestBody User User) {//
        log.info("Добавили нового пользователя");
        return userService.saveUser(User);
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable("id") long id){
        log.info("Удалили пользователя");
        userService.removeUser(id);
    }

    @PatchMapping("/{id}")
    public User updateUser(@RequestBody User user,
                           @PathVariable("id") long id){
        log.info("Обновили пользователя c id: {}", id);
        return userService.updateUser(user, id);
    }
}
