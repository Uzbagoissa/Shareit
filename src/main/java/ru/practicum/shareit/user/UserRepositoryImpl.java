package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final List<Long> blackListID = new ArrayList<>();           //приблуда, чтобы не переиспользовать ID удаленных пользователей

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users.values()) {
            userDtos.add(UserMapper.toUserDto(user));
        }
        return userDtos;
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        emailValid(userDto);
        User user = UserMapper.toUser(userDto, getIdforUser());
        userDto.setId(getIdforUser());
        users.put(user.getId(), user);
        return userDto;
    }

    @Override
    public void removeUser(long id) {
        users.remove(id);
        blackListID.add(id);
    }

    @Override
    public UserDto getUserById(long id) {
        return UserMapper.toUserDto(users.get(id));
    }

    @Override
    public UserDto updateUser(UserDto userDto, long id) {
        emailValid(userDto);
        User user = UserMapper.toUser(userDto, id);
        if (userDto.getEmail() == null) {
            user.setEmail(users.get(id).getEmail());
            userDto.setEmail(users.get(id).getEmail());
        }
        if (userDto.getName() == null) {
            user.setName(users.get(id).getName());
            userDto.setName(users.get(id).getName());
        }
        userDto.setId(id);
        users.put(id, user);
        return userDto;
    }

    private long getIdforUser() {
        long lastId = users.values().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0);
        lastId = lastId + 1;
        for (Long blackID : blackListID) {
            if (lastId == blackID) {
                lastId = lastId + 1;
            }
        }
        return lastId;
    }

    private void emailValid(UserDto userDto) {
        for (User userExist : users.values()) {
            if (userExist.getEmail().equals(userDto.getEmail())) {
                log.error("Пользователь с таким email уже существует! {}", userExist);
                throw new ValidationException("Пользователь с таким email уже существует!");
            }
        }
    }
}
