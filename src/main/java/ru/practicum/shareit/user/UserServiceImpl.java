package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        return repository.getAllUsers();
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        return repository.saveUser(userDto);
    }

    @Override
    public void removeUser(long id) {
        repository.removeUser(id);
    }

    @Override
    public UserDto getUserById(long id) {
        userValid(id);
        return repository.getUserById(id);
    }

    @Override
    public UserDto updateUser(UserDto userDto, long id) {
        userValid(id);
        return repository.updateUser(userDto, id);
    }

    private void userValid(long id) {
        if (!repository.getAllUsers().stream()
                .map(UserDto::getId)
                .collect(Collectors.toList())
                .contains(id)) {
            log.error("Пользователя с таким id не существует! {}", id);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
    }
}