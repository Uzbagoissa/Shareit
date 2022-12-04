package ru.practicum.shareit.user;

import java.util.List;

interface UserRepository {
    List<User> getAllUsers();
    User saveUser(User User);
    void removeUser(long id);
    User getUserById(long id);
    User updateUser(User user, long id);
}