package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        return UserMapper.toListUserDto(repository.findAll());
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        emailValid(userDto);
        return UserMapper.toUserDto(repository.save(UserMapper.toUser(userDto)));
    }

    @Override
    public void removeUser(long id) {
        repository.deleteById(id);
    }

    @Override                                               //в данном случае считаю, что getById() будет лучше, чем findById(),
    public UserDto getUserById(long id) {                   // т.к. в методе getUserById есть валидация id - userValid(id),
        userValid(id);                                      //что исключает выбрасывание NullPointEx при обращении getById() к несуществующему пользователю
        return UserMapper.toUserDto(repository.getById(id));// , а findById() - лишняя возня с Optional
    }

    @Override
    public UserDto updateUser(UserDto userDto, long id) {
        userValid(id);
        emailValid(userDto);
        User user = UserMapper.toUser(userDto);
        if (userDto.getEmail() == null) {
            user.setEmail(repository.getById(id).getEmail());
            userDto.setEmail(repository.getById(id).getEmail());
        }
        if (userDto.getName() == null) {
            user.setName(repository.getById(id).getName());
            userDto.setName(repository.getById(id).getName());
        }
        user.setId(id);
        return UserMapper.toUserDto(repository.save(user));
    }

    private void userValid(long id) {
        if (!repository.findAll().stream()
                .map(User::getId)
                .collect(Collectors.toList())
                .contains(id)) {
            log.error("Пользователя с таким id не существует! {}", id);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
    }

    private void emailValid(UserDto userDto) {
        for (User userExist : repository.findAll()) {
            if (userExist.getEmail().equals(userDto.getEmail())) {
                log.error("Пользователь с таким email уже существует! {}", userExist);
                throw new ValidationException("Пользователь с таким email уже существует!");
            }
        }
    }
}