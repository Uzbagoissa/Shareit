package ru.practicum.shareit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager em;
    @Autowired
    private BookingRepository repository;

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
    void findAllByOwnerId() {
        long ownerId = 1;
        List<Booking> targetBookings = repository.findAllByOwnerId(ownerId);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join Item i on b.itemId=i.id " +
                "where i.userId= :ownerId order by b.end desc ", Booking.class);
        List<Booking> bookings = query.setParameter("ownerId", ownerId).getResultList();
        assertEquals(bookings.size(), targetBookings.size());
        for (Booking booking : bookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", equalTo(booking.getId())),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void findOwnerIdByBookingId() {
        long bookingId = 1;
        Long targetOwnerId = repository.findOwnerIdByBookingId(bookingId);
        TypedQuery<Long> query = em.createQuery("select i.userId from Item as i " +
                "left join Booking as b on b.itemId = i.id where i.id = b.itemId and b.id = :bookingId", Long.class);
        Long ownerId = query.setParameter("bookingId", bookingId).getSingleResult();
        assertNotNull(targetOwnerId);
        assertEquals(ownerId, targetOwnerId);
    }

    @Test
    void findOwnerIdByItemId() {
        long itemId = 1;
        Long targetOwnerId = repository.findOwnerIdByItemId(itemId);
        TypedQuery<Long> query = em.createQuery("select i.userId from Item as i where i.id = :itemId", Long.class);
        Long ownerId = query.setParameter("itemId", itemId).getSingleResult();
        assertNotNull(targetOwnerId);
        assertEquals(ownerId, targetOwnerId);
    }

    @Test
    void findLastBookingByItemId() {
        long itemId = 1;
        LocalDateTime nowTime = LocalDateTime.now();
        Optional<Booking> targetBooking = repository.findLastBookingByItemId(itemId, nowTime);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.itemId = :itemId " +
                "and b.end < :nowTime order by b.end desc ", Booking.class);
        List<Booking> bookings = query.setParameter("itemId", itemId).setParameter("nowTime", nowTime)
                .getResultStream().limit(1).collect(Collectors.toList());
        assertEquals(bookings.get(0).getItemId(), targetBooking.get().getItemId());
        assertEquals(bookings.get(0).getBookerId(), targetBooking.get().getBookerId());
        assertEquals(bookings.get(0).getStart(), targetBooking.get().getStart());
        assertEquals(bookings.get(0).getEnd(), targetBooking.get().getEnd());
        assertEquals(bookings.get(0).getStatus(), targetBooking.get().getStatus());
    }

    @Test
    void findNextBookingByItemId() {
        long itemId = 1;
        LocalDateTime nowTime = LocalDateTime.now();
        Optional<Booking> targetBooking = repository.findNextBookingByItemId(itemId, nowTime);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.itemId = :itemId " +
                "and b.start > :nowTime order by b.start ", Booking.class);
        List<Booking> bookings = query.setParameter("itemId", itemId).setParameter("nowTime", nowTime)
                .getResultStream().limit(1).collect(Collectors.toList());
        assertEquals(bookings.get(0).getItemId(), targetBooking.get().getItemId());
        assertEquals(bookings.get(0).getBookerId(), targetBooking.get().getBookerId());
        assertEquals(bookings.get(0).getStart(), targetBooking.get().getStart());
        assertEquals(bookings.get(0).getEnd(), targetBooking.get().getEnd());
        assertEquals(bookings.get(0).getStatus(), targetBooking.get().getStatus());
    }

    @Test
    void findBookingByUserIdAndItemId() {
        long itemId = 1;
        long bookerId = 2;
        LocalDateTime nowTime = LocalDateTime.now();
        Optional<Booking> targetBooking = repository.findBookingByUserIdAndItemId(bookerId, itemId, nowTime);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.bookerId = :bookerId" +
                " and b.itemId = :itemId and b.end < :nowTime order by b.end desc ", Booking.class);
        List<Booking> bookings = query.setParameter("bookerId", bookerId).setParameter("itemId", itemId)
                .setParameter("nowTime", nowTime).getResultStream().limit(1).collect(Collectors.toList());
        assertEquals(bookings.get(0).getItemId(), targetBooking.get().getItemId());
        assertEquals(bookings.get(0).getBookerId(), targetBooking.get().getBookerId());
        assertEquals(bookings.get(0).getStart(), targetBooking.get().getStart());
        assertEquals(bookings.get(0).getEnd(), targetBooking.get().getEnd());
        assertEquals(bookings.get(0).getStatus(), targetBooking.get().getStatus());
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
