package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;

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
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 1, 'Иван', 'ivan@mail.ru' )");
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 2, 'Петр', 'petr@mail.ru' )");
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 3, 'Вася', 'vase@mail.ru' )");
        jdbcTemplate.update("INSERT INTO USERS VALUES ( 4, 'Вася', 'sdfsdfe@mail.ru' )");

        jdbcTemplate.update("INSERT INTO ITEMS VALUES ( 1, 1, 'метла', 'штука для приборки', true, 1 )");
        jdbcTemplate.update("INSERT INTO ITEMS VALUES ( 2, 1, 'дрель', 'чтоб сверлить', true, 3 )");
        jdbcTemplate.update("INSERT INTO ITEMS VALUES ( 3, 3, 'кофемашина', 'делать кофе', true, 5 )");
        jdbcTemplate.update("INSERT INTO ITEMS VALUES ( 4, 3, 'чайник', 'делать чай', false, 9 )");
        jdbcTemplate.update("INSERT INTO ITEMS VALUES ( 5, 1, 'чайник', 'делать чай', true, 5 )");

        jdbcTemplate.update("INSERT INTO BOOKINGS VALUES ( 1, '2022-3-8 12:30:54'," +
                " '2022-3-10 12:30:54', 1, 2, 'APPROVED' )");
        jdbcTemplate.update("INSERT INTO BOOKINGS VALUES ( 2, '2024-3-8 12:30:54'," +
                " '2024-3-10 12:30:54', 1, 3, 'APPROVED' )");
        jdbcTemplate.update("INSERT INTO BOOKINGS VALUES ( 3, '2024-4-8 12:30:54'," +
                " '2024-4-10 12:30:54', 2, 3, 'WAITING' )");
        jdbcTemplate.update("INSERT INTO BOOKINGS VALUES ( 4, '2023-1-1 12:30:54'," +
                " '2023-4-10 12:30:54', 1, 3, 'APPROVED' )");
        jdbcTemplate.update("INSERT INTO BOOKINGS VALUES ( 5, '2021-1-1 12:30:54'," +
                " '2021-4-10 12:30:54', 2, 3, 'APPROVED' )");
        jdbcTemplate.update("INSERT INTO BOOKINGS VALUES ( 6, '2021-2-1 12:30:54'," +
                " '2021-3-10 12:30:54', 1, 3, 'REJECTED' )");
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
}
