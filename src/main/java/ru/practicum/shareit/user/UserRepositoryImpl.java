package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User saveUser(User user) {
        for (User userExist : users.values()) {
            if (userExist.getEmail().equals(user.getEmail())) {
                log.error("Пользователь с таким email уже существует! {}", userExist);
                throw new ValidationException("Пользователь с таким email уже существует!");
            }
        }
        user.setId(getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void removeUser(long id) {
        users.remove(id);
    }

    @Override
    public User getUserById(long id) {
        if (!users.containsKey(id)) {
            log.error("Пользователя с таким id не существует! {}", id);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
        return users.get(id);
    }

    @Override
    public User updateUser(User user, long id) {
        if (!users.containsKey(id)) {
            log.error("Пользователя с таким id не существует! {}", id);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
        for (User userExist : users.values()) {
            if (userExist.getEmail().equals(user.getEmail())) {
                log.error("Пользователь с таким email уже существует! {}", userExist);
                throw new ValidationException("Пользователь с таким email уже существует!");
            }
        }
        user.setId(id);
        if (user.getName() == null){
            user.setName(users.get(id).getName());
        }
        if (user.getEmail() == null){
            user.setEmail(users.get(id).getEmail());
        }
        users.put(id, user);
        return user;
    }

    private long getId() {
        long lastId = users.values().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0);
        return lastId + 1;
    }
}
