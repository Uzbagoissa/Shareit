package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService service;
    private final JdbcTemplate jdbcTemplate;
    private final long from = 0;
    private final long size = 10;

    @BeforeEach
    void addDate() {
        List<User> sourceUsers = List.of(
                makeUser("ivan@mail.ru", "Иван"),
                makeUser("petr@mail.ru", "Петр"),
                makeUser("vase@mail.ru", "Вася")
        );
        for (User user : sourceUsers) {
            em.persist(user);
        }
        em.flush();

        List<Item> sourceItems = List.of(
                makeItemForDB(1L, "метла", "штука для приборки", true, 1L),
                makeItemForDB(1L, "дрель", "чтоб сверлить", true, 3L),
                makeItemForDB(3L, "кофемашина", "делать кофе", true, 5L)
        );
        for (Item item : sourceItems) {
            em.persist(item);
        }
        em.flush();

        List<Booking> sourceBookings = List.of(
                makeBooking(LocalDateTime.of(2022, 3, 8, 12, 30, 54),
                        LocalDateTime.of(2022, 3, 9, 12, 30, 54),
                        2L, 3L, BookingStatus.APPROVED)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("DELETE FROM ITEMS");
        jdbcTemplate.update("DELETE FROM BOOKINGS");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE ITEMS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE BOOKINGS ALTER COLUMN ID RESTART WITH 1");
    }

    @Test
    void saveItem() {
        long userId = 2;
        ItemDto itemDto = service.saveItem(userId,
                makeItemDto("камера", "делать видео", true, 2L));
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item item = query.setParameter("name", itemDto.getName()).getSingleResult();
        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(item.getRequestId(), equalTo(itemDto.getRequestId()));
    }

    @Test
    void saveItemWithoutAvailable() {
        long userId = 2;
        ItemDto itemDto = makeItemDto("камера", "делать видео", null, 2L);
        assertThrows(IncorrectParameterException.class, () -> {
            service.saveItem(userId, itemDto);
        });
    }

    @Test
    void getAllItems() {
        long userId = 1;
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.userId = :userId", Item.class);
        List<Item> items = query.setParameter("userId", userId).getResultList();
        List<ItemDto> targetItems = service.getAllItems(userId, from, size);
        assertEquals(targetItems.size(), 2);
        assertThat(targetItems, hasSize(items.size()));
        for (Item sourceItem : items) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable())),
                    hasProperty("requestId", equalTo(sourceItem.getRequestId()))
            )));
        }
    }

    @Test
    void getItemById() {
        long userId = 1;
        long itemId = 2;
        long authorOfCommentId = 3;
        String text = "ниче такая штуковина, пойдет";
        CommentDto commentDto = new CommentDto();
        commentDto.setText(text);
        service.saveComment(authorOfCommentId, commentDto, itemId);
        ItemDto itemDto = service.getItemById(userId, itemId);
        TypedQuery<Item> queryItem = em.createQuery("Select i from Item i where i.id= :itemId", Item.class);
        Item item = queryItem.setParameter("itemId", itemId).getSingleResult();
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertEquals(item.getRequestId(), itemDto.getRequestId());

        TypedQuery<Comment> queryBooking = em.createQuery("Select c from Comment c where c.item= :itemId" +
                " and c.author= :authorOfCommentId", Comment.class);
        List<Comment> comments = queryBooking.setParameter("itemId", itemId).setParameter("authorOfCommentId",
                authorOfCommentId).getResultList();
        List<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : comments) {
            commentDtos.add(CommentMapper.toCommentDto(comment));
        }
        assertEquals(commentDtos.get(0).getId(), itemDto.getComments().get(0).getId());
        assertEquals(commentDtos.get(0).getCreated(), itemDto.getComments().get(0).getCreated());
        assertEquals(commentDtos.get(0).getText(), itemDto.getComments().get(0).getText());
    }

    @Test
    void searchItems() {
        String text1 = "еТл";
        String text2 = "СверЛ";
        String text3 = "коф";
        List<ItemDto> targetItems1 = service.searchItems(text1, from, size);
        TypedQuery<Item> query1 = em.createQuery("Select i from Item i where i.id= 1", Item.class);
        Item item = query1.getSingleResult();
        assertEquals(targetItems1.get(0).getId(), item.getId());

        List<ItemDto> targetItems2 = service.searchItems(text2, from, size);
        TypedQuery<Item> query2 = em.createQuery("Select i from Item i where i.id= 2", Item.class);
        item = query2.getSingleResult();
        assertEquals(targetItems2.get(0).getId(), item.getId());

        List<ItemDto> targetItems3 = service.searchItems(text3, from, size);
        TypedQuery<Item> query3 = em.createQuery("Select i from Item i where i.id= 3", Item.class);
        item = query3.getSingleResult();
        assertEquals(targetItems3.get(0).getId(), item.getId());
    }

    @Test
    void updateItem() {
        long userId = 1;
        long itemId = 1;
        ItemDto itemDto1 = service.updateItem(userId, makeItemDto("веник", "штука для приборки",
                true, 1L), itemId);
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id= :itemId", Item.class);
        Item item = query.setParameter("itemId", itemId).getSingleResult();
        assertEquals(item.getName(), itemDto1.getName());

        ItemDto itemDto2 = service.updateItem(userId, makeItemDto(null, "штуковина чтобы чисто",
                true, 1L), itemId);
        query = em.createQuery("Select i from Item i where i.id= :itemId", Item.class);
        item = query.setParameter("itemId", itemId).getSingleResult();
        assertEquals(item.getName(), "веник");
        assertEquals(item.getDescription(), itemDto2.getDescription());

        ItemDto itemDto3 = service.updateItem(userId, makeItemDto("веник", null, true,
                1L), itemId);
        query = em.createQuery("Select i from Item i where i.id= :itemId", Item.class);
        item = query.setParameter("itemId", itemId).getSingleResult();
        assertEquals(item.getDescription(), "штуковина чтобы чисто");
        assertEquals(item.getName(), itemDto3.getName());

        ItemDto itemDto4 = service.updateItem(userId, makeItemDto("веник", "штуковина чтобы чисто",
                null, 1L), itemId);
        query = em.createQuery("Select i from Item i where i.id= :itemId", Item.class);
        item = query.setParameter("itemId", itemId).getSingleResult();
        assertEquals(item.getAvailable(), true);
        assertEquals(item.getName(), itemDto4.getName());
    }

    /*попытка изменить вещь пользователем, который не является ее владельцем*/
    @Test
    void updateItemByIncorrectUser() {
        long userId = 1;
        long itemId = 3;
        ItemDto itemDto = makeItemDto("веник", "штука для приборки", true, 1L);
        assertThrows(ForbiddenException.class, () -> {
            service.updateItem(userId, itemDto, itemId);
        });
    }

    @Test
    void saveComment() {
        long userId = 3;
        long itemId = 2;
        String text = "ниче такая штуковина, пойдет";
        CommentDto commentDto = new CommentDto();
        commentDto.setText(text);
        CommentDto commentDtoCheck = service.saveComment(userId, commentDto, itemId);
        TypedQuery<Comment> query = em.createQuery("Select c from Comment c where c.item= :itemId" +
                " and c.author= :userId", Comment.class);
        Comment comment = query.setParameter("itemId", itemId).setParameter("userId", userId).getSingleResult();
        assertEquals(commentDtoCheck.getId(), comment.getId());
        assertEquals(commentDtoCheck.getText(), comment.getText());
    }

    /*попытка оставить комментарий пользователем, который не бронировал вещь*/
    @Test
    void saveCommentByIncorrectUser() {
        long userId = 2;
        long itemId = 2;
        String text = "ниче такая штуковина, пойдет";
        CommentDto commentDto = new CommentDto();
        commentDto.setText(text);
        assertThrows(IncorrectParameterException.class, () -> {
            service.saveComment(userId, commentDto, itemId);
        });
    }

    /*получение несуществующей вещи*/
    @Test
    void itemValid() {
        assertThrows(NotFoundException.class, () -> {
            service.getItemById(1, 8);
        });
    }

    private Item makeItemForDB(Long userId, String name, String description, Boolean available, Long requestId) {
        Item item = new Item();
        item.setUserId(userId);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setRequestId(requestId);
        return item;
    }

    private static ItemDto makeItemDto(String name, String description, Boolean available, Long requestId) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        return itemDto;
    }

    private User makeUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, Long itemId, Long bookerId, BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItemId(itemId);
        booking.setBookerId(bookerId);
        booking.setStatus(status);
        return booking;
    }
}
