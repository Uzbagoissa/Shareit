package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    List<User> getAllUsers();
    User saveUser(User User);
    void removeUser(long id);
    User getUserById(long id);
    User updateUser(User user, long id);
}