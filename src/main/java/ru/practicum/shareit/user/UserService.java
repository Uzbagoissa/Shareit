package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

interface UserService {
    List<User> getAllUsers();
    User saveUser(User User);
    void removeUser(long id);
    User getUserById(long id);
    User updateUser(User user, long id);
}