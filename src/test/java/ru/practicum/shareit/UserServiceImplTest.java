package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    private final EntityManager em;
    private final UserService service;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void saveUser() {
        UserDto userDto = makeUserDto("ivanko@mail.ru", "Иваныч");
        service.saveUser(userDto);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();
        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getAllUsers() {
        TypedQuery<User> query = em.createQuery("Select u from User u", User.class);
        List<User> users = query.getResultList();
        List<UserDto> targetUsers = service.getAllUsers();
        assertThat(targetUsers, hasSize(users.size()));
        for (User sourceUser : users) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(toUserDto(sourceUser).getName())),
                    hasProperty("email", equalTo(toUserDto(sourceUser).getEmail()))
            )));
        }
    }

    @Test
    void removeUser() {
        TypedQuery<User> query = em.createQuery("Select u from User u", User.class);
        List<User> users = query.getResultList();
        service.removeUser(2);
        TypedQuery<User> queryAfterRemove = em.createQuery("Select u from User u", User.class);
        List<User> usersAfterRemove = queryAfterRemove.getResultList();
        assertEquals(usersAfterRemove.size(), users.size() - 1);
        assertThat(usersAfterRemove, hasSize(2));
        assertEquals(usersAfterRemove.get(0).getEmail(), "ivan@mail.ru");
        assertEquals(usersAfterRemove.get(1).getEmail(), "vase@mail.ru");
    }

    @Test
    void getUserById() {
        UserDto userDto = service.getUserById(2);
        assertEquals(userDto.getEmail(), "petr@mail.ru");
    }

    @Test
    void updateUser() {
        UserDto userDto = makeUserDto("kent@mail.ru", "Петр");
        service.updateUser(userDto, 2);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = 2", User.class);
        User user = query.getSingleResult();
        assertEquals(user.getName(), "Петр");
        assertEquals(user.getEmail(), "kent@mail.ru");
        UserDto userDto2 = makeUserDto(null, "Ваня");
        service.updateUser(userDto2, 1);
        query = em.createQuery("Select u from User u where u.id = 1", User.class);
        User user2 = query.getSingleResult();
        assertEquals(user2.getName(), "Ваня");
        assertEquals(user2.getEmail(), "ivan@mail.ru");
        UserDto userDto3 = makeUserDto("ivanich@mail.ru", null);
        service.updateUser(userDto3, 1);
        query = em.createQuery("Select u from User u where u.id = 1", User.class);
        User user3 = query.getSingleResult();
        assertEquals(user3.getName(), "Ваня");
        assertEquals(user3.getEmail(), "ivanich@mail.ru");
    }

    @Test
    void userValid() {
        assertThrows(NotFoundException.class, () -> {service.getUserById(8);});
    }

    @BeforeEach
    void addDate() {
        List<UserDto> sourceUsers = List.of(
                makeUserDto("ivan@mail.ru", "Иван"),
                makeUserDto("petr@mail.ru", "Петр"),
                makeUserDto("vase@mail.ru", "Вася")
        );

        for (UserDto userDto : sourceUsers) {
            User entity = toUser(userDto);
            em.persist(entity);
        }
        em.flush();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
    }

    private UserDto makeUserDto(String email, String name) {
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setName(name);
        return userDto;
    }

    private User toUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

}
