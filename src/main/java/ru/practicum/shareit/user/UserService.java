package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto saveUser(UserDto userDto);
    void removeUser(long id);
    UserDto getUserById(long id);
    UserDto updateUser(UserDto userDto, long id);
}