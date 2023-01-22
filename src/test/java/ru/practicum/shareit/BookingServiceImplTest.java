package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
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
public class BookingServiceImplTest {
    private final EntityManager em;
    private final BookingService service;
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
                makeItem(1L, "метла", "штука для приборки", true, 1L),
                makeItem(1L, "дрель", "чтоб сверлить", true, 3L),
                makeItem(3L, "кофемашина", "делать кофе", true, 5L),
                makeItem(3L, "чайник", "делать чай", false, 9L)

        );
        for (Item item : sourceItems) {
            em.persist(item);
        }
        em.flush();

        List<Booking> sourceBookings = List.of(
                makeBooking(LocalDateTime.of(2022, 3, 8, 12, 30, 54),
                        LocalDateTime.of(2022, 3, 10, 12, 30, 54),
                        1L, 2L, BookingStatus.APPROVED),
                makeBooking(LocalDateTime.of(2024, 3, 8, 12, 30, 54),
                        LocalDateTime.of(2024, 3, 10, 12, 30, 54),
                        1L, 3L, BookingStatus.APPROVED),
                makeBooking(LocalDateTime.of(2024, 4, 8, 12, 30, 54),
                        LocalDateTime.of(2024, 4, 10, 12, 30, 54),
                        2L, 3L, BookingStatus.WAITING),
                makeBooking(LocalDateTime.of(2023, 1, 1, 12, 30, 54),
                        LocalDateTime.of(2023, 4, 10, 12, 30, 54),
                        1L, 3L, BookingStatus.APPROVED),
                makeBooking(LocalDateTime.of(2021, 1, 1, 12, 30, 54),
                        LocalDateTime.of(2021, 4, 10, 12, 30, 54),
                        2L, 3L, BookingStatus.APPROVED),
                makeBooking(LocalDateTime.of(2021, 2, 1, 12, 30, 54),
                        LocalDateTime.of(2021, 3, 10, 12, 30, 54),
                        1L, 3L, BookingStatus.REJECTED)
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
    void saveBooking() {
        long bookerId = 2;
        long itemId = 3;
        LocalDateTime bookingStart = LocalDateTime.of(2024, 5, 8, 12, 30, 54);
        LocalDateTime bookingEnd = LocalDateTime.of(2024, 5, 10, 12, 30, 54);
        BookingDtoOut bookingDtoOut = service.saveBooking(bookerId, new BookingDtoIn(itemId, bookingStart, bookingEnd));
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.itemId= :itemId " +
                "and b.start= :bookingStart and b.end= :bookingEnd and b.bookerId= :bookerId", Booking.class);
        Booking booking = query.setParameter("itemId", itemId).setParameter("bookingStart", bookingStart)
                .setParameter("bookingEnd", bookingEnd).setParameter("bookerId", bookerId).getSingleResult();
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStart(), equalTo(bookingDtoOut.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDtoOut.getEnd()));
        assertThat(booking.getItemId(), equalTo(bookingDtoOut.getItem().getId()));
        assertThat(booking.getBookerId(), equalTo(bookingDtoOut.getBooker().getId()));
        assertThat(booking.getStatus(), equalTo(bookingDtoOut.getStatus()));
    }

    @Test
    void updateBooking() {
        long ownerId = 1;
        long bookingId1 = 3;
        String approved1 = "false";
        BookingDtoOut bookingDtoOut1 = service.updateBooking(ownerId, approved1, bookingId1);
        TypedQuery<Booking> query1 = em.createQuery("Select b from Booking b where b.id= :bookingId ", Booking.class);
        Booking booking1 = query1.setParameter("bookingId", bookingId1).getSingleResult();
        assertThat(booking1.getId(), equalTo(bookingDtoOut1.getId()));
        assertThat(booking1.getStart(), equalTo(bookingDtoOut1.getStart()));
        assertThat(booking1.getEnd(), equalTo(bookingDtoOut1.getEnd()));
        assertThat(booking1.getItemId(), equalTo(bookingDtoOut1.getItem().getId()));
        assertThat(booking1.getBookerId(), equalTo(bookingDtoOut1.getBooker().getId()));
        assertThat(booking1.getStatus(), equalTo(bookingDtoOut1.getStatus()));

        long bookingId2 = 6;
        String approved2 = "true";
        BookingDtoOut bookingDtoOut2 = service.updateBooking(ownerId, approved2, bookingId2);
        TypedQuery<Booking> query2 = em.createQuery("Select b from Booking b where b.id= :bookingId ", Booking.class);
        Booking booking2 = query2.setParameter("bookingId", bookingId2).getSingleResult();
        assertThat(booking2.getId(), equalTo(bookingDtoOut2.getId()));
        assertThat(booking2.getStatus(), equalTo(bookingDtoOut2.getStatus()));
    }

    @Test
    void getBookingById() {
        long userId = 1;
        long bookingId = 2;
        BookingDtoOut bookingDtoOut = service.getBookingById(userId, bookingId);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id= :bookingId", Booking.class);
        Booking booking = query.setParameter("bookingId", bookingId).getSingleResult();
        assertEquals(booking.getId(), bookingDtoOut.getId());
        assertEquals(booking.getStart(), bookingDtoOut.getStart());
        assertEquals(booking.getEnd(), bookingDtoOut.getEnd());
        assertEquals(booking.getItemId(), bookingDtoOut.getItem().getId());
        assertEquals(booking.getBookerId(), bookingDtoOut.getBooker().getId());
        assertEquals(booking.getStatus(), bookingDtoOut.getStatus());
    }

    @Test
    void getALLBookingsByBookerId() {
        long bookerId = 3;
        String state = "ALL";
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByBookerId(bookerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.bookerId= :bookerId", Booking.class);
        List<Booking> bookings = query.setParameter("bookerId", bookerId).getResultList();
        assertEquals(bookingDtoOuts.size(), 5);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getCURRENTBookingsByBookerId() {
        long bookerId = 3;
        LocalDateTime nowTime = LocalDateTime.now();
        String state = "CURRENT";
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByBookerId(bookerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.bookerId= :bookerId " +
                "and b.start< :nowTime and  b.end> :nowTime ", Booking.class);
        List<Booking> bookings = query.setParameter("bookerId", bookerId).setParameter("nowTime",
                nowTime).getResultList();
        assertEquals(bookingDtoOuts.size(), 1);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getPASTBookingsByBookerId() {
        long bookerId = 3;
        LocalDateTime nowTime = LocalDateTime.now();
        String state = "PAST";
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByBookerId(bookerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.bookerId= :bookerId " +
                "and b.end< :nowTime ", Booking.class);
        List<Booking> bookings = query.setParameter("bookerId", bookerId).setParameter("nowTime",
                nowTime).getResultList();
        assertEquals(bookingDtoOuts.size(), 2);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getFUTUREBookingsByBookerId() {
        long bookerId = 3;
        LocalDateTime nowTime = LocalDateTime.now();
        String state = "FUTURE";
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByBookerId(bookerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.bookerId= :bookerId " +
                "and b.start> :nowTime ", Booking.class);
        List<Booking> bookings = query.setParameter("bookerId", bookerId).setParameter("nowTime",
                nowTime).getResultList();
        assertEquals(bookingDtoOuts.size(), 2);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getWAITINGBookingsByBookerId() {
        long bookerId = 3;
        String state = "WAITING";
        BookingStatus bookingState = BookingStatus.WAITING;
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByBookerId(bookerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.bookerId= :bookerId " +
                "and b.status= :bookingState ", Booking.class);
        List<Booking> bookings = query.setParameter("bookerId", bookerId).setParameter("bookingState",
                bookingState).getResultList();
        assertEquals(bookingDtoOuts.size(), 1);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getREJECTEDBookingsByBookerId() {
        long bookerId = 3;
        String state = "REJECTED";
        BookingStatus bookingState = BookingStatus.REJECTED;
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByBookerId(bookerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.bookerId= :bookerId " +
                "and b.status= :bookingState ", Booking.class);
        List<Booking> bookings = query.setParameter("bookerId", bookerId).setParameter("bookingState",
                bookingState).getResultList();
        assertEquals(bookingDtoOuts.size(), 1);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getALLBookingsByOwnerId() {
        long ownerId = 1;
        String state = "ALL";
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByOwnerId(ownerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join Item i on b.itemId=i.id " +
                "where i.userId= :ownerId order by b.end desc ", Booking.class);
        List<Booking> bookings = query.setParameter("ownerId", ownerId).getResultList();
        assertEquals(bookingDtoOuts.size(), 6);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getCURRENTBookingsByOwnerId() {
        long ownerId = 1;
        String state = "CURRENT";
        LocalDateTime nowTime = LocalDateTime.now();
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByOwnerId(ownerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join Item i on b.itemId=i.id " +
                "where i.userId= :ownerId and b.start< :nowTime and  b.end> :nowTime", Booking.class);
        List<Booking> bookings = query.setParameter("ownerId", ownerId).setParameter("nowTime",
                nowTime).getResultList();
        assertEquals(bookingDtoOuts.size(), 1);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getPASTBookingsByOwnerId() {
        long ownerId = 1;
        String state = "PAST";
        LocalDateTime nowTime = LocalDateTime.now();
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByOwnerId(ownerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join Item i on b.itemId=i.id " +
                "where i.userId= :ownerId and b.end< :nowTime", Booking.class);
        List<Booking> bookings = query.setParameter("ownerId", ownerId).setParameter("nowTime",
                nowTime).getResultList();
        assertEquals(bookingDtoOuts.size(), 3);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getFUTUREBookingsByOwnerId() {
        long ownerId = 1;
        String state = "FUTURE";
        LocalDateTime nowTime = LocalDateTime.now();
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByOwnerId(ownerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join Item i on b.itemId=i.id " +
                "where i.userId= :ownerId and b.start> :nowTime", Booking.class);
        List<Booking> bookings = query.setParameter("ownerId", ownerId).setParameter("nowTime",
                nowTime).getResultList();
        assertEquals(bookingDtoOuts.size(), 2);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getWAITINGBookingsByOwnerId() {
        long ownerId = 1;
        String state = "WAITING";
        BookingStatus bookingState = BookingStatus.WAITING;
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByOwnerId(ownerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join Item i on b.itemId=i.id " +
                "where i.userId= :ownerId and b.status= :bookingState", Booking.class);
        List<Booking> bookings = query.setParameter("ownerId", ownerId).setParameter("bookingState",
                bookingState).getResultList();
        assertEquals(bookingDtoOuts.size(), 1);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getREJECTEDBookingsByOwnerId() {
        long ownerId = 1;
        String state = "REJECTED";
        BookingStatus bookingState = BookingStatus.REJECTED;
        List<BookingDtoOut> bookingDtoOuts = service.getAllBookingsByOwnerId(ownerId, state, from, size);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join Item i on b.itemId=i.id " +
                "where i.userId= :ownerId and b.status= :bookingState", Booking.class);
        List<Booking> bookings = query.setParameter("ownerId", ownerId).setParameter("bookingState",
                bookingState).getResultList();
        assertEquals(bookingDtoOuts.size(), 1);
        assertEquals(bookings.size(), bookingDtoOuts.size());
        for (Booking booking : bookings) {
            assertThat(bookingDtoOuts, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void getAllBookingsByOwnerIdWithIncorrectState() {
        long ownerId = 1;
        String state = "KHGJKG";
        assertThrows(IncorrectParameterException.class,
                () -> {
                    service.getAllBookingsByOwnerId(ownerId, state, from, size);
                });
    }

    /*попытка забронировать вещь, которой нет в наличии*/
    @Test
    void saveBookingOfItemIsNotAvailable() {
        long bookerId = 2;
        long itemId = 4;
        LocalDateTime bookingStart = LocalDateTime.of(2023, 5, 8, 12, 30, 54);
        LocalDateTime bookingEnd = LocalDateTime.of(2023, 5, 10, 12, 30, 54);
        assertThrows(IncorrectParameterException.class,
                () -> {
                    service.saveBooking(bookerId, new BookingDtoIn(itemId, bookingStart, bookingEnd));
                });
    }

    @Test
    void saveBookingWithIncorrectTime() {
        long bookerId1 = 2;
        long itemId = 2;
        /*время окончания бронирования указано в прошлом*/
        LocalDateTime bookingStart1 = LocalDateTime.of(2023, 1, 8, 12, 30, 54);
        LocalDateTime bookingEnd1 = LocalDateTime.of(2023, 1, 10, 12, 30, 54);
        assertThrows(IncorrectParameterException.class,
                () -> {
                    service.saveBooking(bookerId1, new BookingDtoIn(itemId, bookingStart1, bookingEnd1));
                });

        /*Время окончания аренды раньше времени начала аренды*/
        LocalDateTime bookingStart2 = LocalDateTime.of(2025, 5, 20, 12, 30, 54);
        LocalDateTime bookingEnd2 = LocalDateTime.of(2025, 5, 18, 12, 30, 54);
        assertThrows(IncorrectParameterException.class,
                () -> {
                    service.saveBooking(bookerId1, new BookingDtoIn(itemId, bookingStart2, bookingEnd2));
                });

        /*время начала бронирования указано в прошлом*/
        LocalDateTime bookingStart3 = LocalDateTime.of(2023, 1, 1, 12, 30, 54);
        LocalDateTime bookingEnd3 = LocalDateTime.of(2025, 5, 18, 12, 30, 54);
        assertThrows(IncorrectParameterException.class,
                () -> {
                    service.saveBooking(bookerId1, new BookingDtoIn(itemId, bookingStart3, bookingEnd3));
                });
    }

    /*владелец вещи пытается забронировать свою вещь*/
    @Test
    void saveBookingWithIncorrectBooker() {
        long bookerId2 = 1;
        long itemId = 2;
        LocalDateTime bookingStart4 = LocalDateTime.of(2025, 1, 1, 12, 30, 54);
        LocalDateTime bookingEnd4 = LocalDateTime.of(2025, 5, 18, 12, 30, 54);
        assertThrows(NotFoundException.class,
                () -> {
                    service.saveBooking(bookerId2, new BookingDtoIn(itemId, bookingStart4, bookingEnd4));
                });
    }

    /*получение бронирование пользователем, у которого нет на это прав*/
    @Test
    void getBookingByIdByIncorrectUser() {
        long userId = 2;
        long bookingId = 2;
        assertThrows(NotFoundException.class,
                () -> {
                    service.getBookingById(userId, bookingId);
                });
    }

    /*изменение бронирование пользователем, который не является владельцем вещи*/
    @Test
    void updateBookingByIncorrectUser() {
        long userId1 = 2;
        long bookingId1 = 3;
        String approved1 = "true";
        assertThrows(NotFoundException.class,
                () -> {
                    service.updateBooking(userId1, approved1, bookingId1);
                });

        long userId2 = 3;
        assertThrows(NotFoundException.class,
                () -> {
                    service.updateBooking(userId2, approved1, bookingId1);
                });
    }

    /*изменение статуса бронирования владельцем вещи после того, как статус стал APPROVED*/
    @Test
    void updateBookingByIncorrectStatus() {
        long ownerId = 1;
        long bookingId = 1;
        String approved = "false";
        assertThrows(IncorrectParameterException.class,
                () -> {
                    service.updateBooking(ownerId, approved, bookingId);
                });
    }

    /*получение несуществующего бронирования*/
    @Test
    void bookingValid() {
        assertThrows(NotFoundException.class, () -> {
            service.getBookingById(1, 8);
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
