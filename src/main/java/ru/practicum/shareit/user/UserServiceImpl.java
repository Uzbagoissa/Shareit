package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {
        return repository.getAllUsers();
    }

    @Override
    public User saveUser(User User) {
        return repository.saveUser(User);
    }

    @Override
    public void removeUser(long id) {
        repository.removeUser(id);
    }

    @Override
    public User getUserById(long id) {
        return repository.getUserById(id);
    }

    @Override
    public User updateUser(User user, long id) {
        return repository.updateUser(user, id);
    }
}