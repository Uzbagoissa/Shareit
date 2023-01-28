package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @BeforeEach
    void addDate() {
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 1, 'Иван', 'ivan@mail.ru' )");
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 2, 'Петр', 'petr@mail.ru' )");
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 3, 'Вася', 'vase@mail.ru' )");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
    }

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
        assertEquals(targetUsers.size(), 3);
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
        assertEquals(usersAfterRemove.get(0).getName(), "Иван");
        assertEquals(usersAfterRemove.get(0).getEmail(), "ivan@mail.ru");
        assertEquals(usersAfterRemove.get(1).getName(), "Вася");
        assertEquals(usersAfterRemove.get(1).getEmail(), "vase@mail.ru");
    }

    @Test
    void getUserById() {
        UserDto userDto = service.getUserById(2);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = 2", User.class);
        User user = query.getSingleResult();
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    void updateUser() {
        UserDto userDto = service.updateUser(makeUserDto("kent@mail.ru", "Петр"), 2);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = 2", User.class);
        User user = query.getSingleResult();
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());

        UserDto userDto2 = service.updateUser(makeUserDto(null, "Ваня"), 1);
        query = em.createQuery("Select u from User u where u.id = 1", User.class);
        User user2 = query.getSingleResult();
        assertEquals(user2.getName(), userDto2.getName());
        assertEquals(user2.getEmail(), userDto2.getEmail());

        UserDto userDto3 = service.updateUser(makeUserDto("ivanich@mail.ru", null), 1);
        query = em.createQuery("Select u from User u where u.id = 1", User.class);
        User user3 = query.getSingleResult();
        assertEquals(user3.getName(), userDto3.getName());
        assertEquals(user3.getEmail(), userDto3.getEmail());
    }

    /*получение несуществующего пользователя*/
    @Test
    void userValid() {
        assertThrows(NotFoundException.class, () -> {
            service.getUserById(8);
        });
    }

    private UserDto makeUserDto(String email, String name) {
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setName(name);
        return userDto;
    }

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

}
