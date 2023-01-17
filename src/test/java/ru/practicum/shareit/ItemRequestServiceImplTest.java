package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
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
public class ItemRequestServiceImplTest {
    private final EntityManager em;
    private final ItemRequestService service;
    private final JdbcTemplate jdbcTemplate;
    private final long from = 0;
    private final long size = 10;
    private List<Item> sourceItems;

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

        List<ItemRequest> sourceItemRequests = List.of(
                makeItemRequest("нужна штука, чтобы делать чисто", 2L,
                        LocalDateTime.of(2023, 1, 8, 12, 30, 54)),
                makeItemRequest("нужна штука, чтобы сверлить", 3L,
                        LocalDateTime.of(2023, 1, 12, 12, 30, 54)),
                makeItemRequest("нужна штука, чтобы варить кофе", 3L,
                        LocalDateTime.of(2023, 1, 15, 12, 30, 54)),
                makeItemRequest("нужна штука, чтобы выгуливать кота", 3L,
                        LocalDateTime.of(2023, 1, 16, 12, 30, 54)),
                makeItemRequest("нужна штука, чтобы летать", 2L,
                        LocalDateTime.of(2023, 1, 17, 12, 30, 54))

        );
        for (ItemRequest itemRequest : sourceItemRequests) {
            em.persist(itemRequest);
        }
        em.flush();

        sourceItems = List.of(
                makeItem(1L, "метла", "штука для приборки", true, 1L),
                makeItem(1L, "дрель", "чтоб сверлить", true, 2L),
                makeItem(3L, "кофемашина", "делать кофе", true, 3L)
        );
        for (Item item : sourceItems) {
            em.persist(item);
        }
        em.flush();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("DELETE FROM ITEMS");
        jdbcTemplate.update("DELETE FROM REQUESTS");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE ITEMS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE REQUESTS ALTER COLUMN ID RESTART WITH 1");
    }

    @Test
    void saveItemRequest() {
        long requesterId = 1;
        String description = "нужна штука, чтобы снимать фото";
        ItemRequestDtoOut itemRequestDtoOut = service.saveItemRequest(requesterId, new ItemRequestDtoIn(description));
        TypedQuery<ItemRequest> query = em.createQuery("Select ir from ItemRequest ir where ir.requesterId=" +
                " :requesterId and ir.description= :description", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("requesterId", requesterId)
                .setParameter("description", description).getSingleResult();
        assertThat(itemRequest.getId(), notNullValue());
        assertThat(itemRequest.getDescription(), equalTo(itemRequestDtoOut.getDescription()));
        assertThat(itemRequest.getCreated(), equalTo(itemRequestDtoOut.getCreated()));
    }

    @Test
    void getAllItemRequestByRequesterId() {
        long requesterId = 3;
        List<ItemRequestDtoOut> itemRequestDtoOuts = service.getAllItemRequestByRequesterId(requesterId);
        TypedQuery<ItemRequest> query = em.createQuery("Select ir from ItemRequest ir where ir.requesterId=" +
                " :requesterId order by ir.created desc ", ItemRequest.class);
        List<ItemRequest> itemRequests = query.setParameter("requesterId", requesterId).getResultList();
        assertEquals(itemRequestDtoOuts.size(), 3);
        assertEquals(itemRequests.size(), itemRequestDtoOuts.size());
        for (ItemRequest itemRequest : itemRequests) {
            assertThat(itemRequestDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(itemRequest.getId())),
                    hasProperty("description", equalTo(itemRequest.getDescription())),
                    hasProperty("created", equalTo(itemRequest.getCreated()))
            )));
        }
        assertEquals(itemRequestDtoOuts.get(0).getItems().get(0), sourceItems.get(1));
        assertEquals(itemRequestDtoOuts.get(1).getItems().get(0), sourceItems.get(2));
    }

    @Test
    void getAllItemRequest() {
        long userId = 1;
        List<ItemRequestDtoOut> itemRequestDtoOuts = service.getAllItemRequest(userId, from, size);
        TypedQuery<ItemRequest> query = em.createQuery("Select ir from ItemRequest ir where ir.requesterId<>" +
                " :userId order by ir.created desc ", ItemRequest.class);
        List<ItemRequest> itemRequests = query.setParameter("userId", userId).getResultList();
        assertEquals(itemRequestDtoOuts.size(), 5);
        assertEquals(itemRequests.size(), itemRequestDtoOuts.size());
        for (ItemRequest itemRequest : itemRequests) {
            assertThat(itemRequestDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(itemRequest.getId())),
                    hasProperty("description", equalTo(itemRequest.getDescription())),
                    hasProperty("created", equalTo(itemRequest.getCreated()))
            )));
        }
        assertEquals(itemRequestDtoOuts.get(0).getItems().get(0), sourceItems.get(0));
        assertEquals(itemRequestDtoOuts.get(1).getItems().get(0), sourceItems.get(1));
        assertEquals(itemRequestDtoOuts.get(2).getItems().get(0), sourceItems.get(2));
    }

    @Test
    void getItemRequestById() {
        long userId = 1;
        long itemRequestId = 1;
        ItemRequestDtoOut itemRequestDtoOut = service.getItemRequestById(userId, itemRequestId);
        TypedQuery<ItemRequest> query = em.createQuery("Select ir from ItemRequest ir where ir.id=" +
                " :itemRequestId", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("itemRequestId", itemRequestId).getSingleResult();
        assertEquals(itemRequest.getId(), itemRequestDtoOut.getId());
        assertEquals(itemRequest.getDescription(), itemRequestDtoOut.getDescription());
        assertEquals(itemRequest.getCreated(), itemRequestDtoOut.getCreated());
    }

    /*получение несуществующего запроса*/
    @Test
    void itemRequestValid() {
        assertThrows(NotFoundException.class, () -> {
            service.getItemRequestById(1L, 8L);
        });
    }

    private User makeUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Item makeItem(Long userId, String name, String description, Boolean available, Long requestId) {
        Item item = new Item();
        item.setUserId(userId);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setRequestId(requestId);
        return item;
    }

    private ItemRequest makeItemRequest(String description, Long requesterId, LocalDateTime created) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(description);
        itemRequest.setRequesterId(requesterId);
        itemRequest.setCreated(created);
        return itemRequest;
    }
}
