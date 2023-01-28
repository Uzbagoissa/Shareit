package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
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

    @BeforeEach
    void addDate() {
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 1, 'Иван', 'ivan@mail.ru' )");
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 2, 'Петр', 'petr@mail.ru' )");
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 3, 'Вася', 'vase@mail.ru' )");

        jdbcTemplate.update("INSERT INTO REQUESTS VALUES ( 1, 'нужна штука, чтобы делать чисто', 2, " +
                "'2023-1-8 12:30:54' )");
        jdbcTemplate.update("INSERT INTO REQUESTS VALUES ( 2, 'нужна штука, чтобы сверлить', 3, " +
                "'2023-1-12 12:30:54' )");
        jdbcTemplate.update("INSERT INTO REQUESTS VALUES ( 3, 'нужна штука, чтобы варить кофе', 3, " +
                "'2023-1-15 12:30:54' )");
        jdbcTemplate.update("INSERT INTO REQUESTS VALUES ( 4, 'нужна штука, чтобы выгуливать кота', 3, " +
                "'2023-1-16 12:30:54' )");
        jdbcTemplate.update("INSERT INTO REQUESTS VALUES ( 5, 'нужна штука, чтобы летать', 2, " +
                "'2023-1-17 12:30:54' )");

        jdbcTemplate.update("INSERT INTO ITEMS VALUES ( 1, 1, 'метла', 'штука для приборки', true, 1 )");
        jdbcTemplate.update("INSERT INTO ITEMS VALUES ( 2, 1, 'дрель', 'чтоб сверлить', true, 3 )");
        jdbcTemplate.update("INSERT INTO ITEMS VALUES ( 3, 3, 'кофемашина', 'делать кофе', true, 5 )");
    }

    @Test
    void saveItemRequest() {
        jdbcTemplate.update("DELETE FROM REQUESTS");
        jdbcTemplate.update("ALTER TABLE REQUESTS ALTER COLUMN ID RESTART WITH 1");
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
}
