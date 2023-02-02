package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdOrderByEndDesc(long bookerId);

    @Query(value = "select b.* " +
            "from bookings as b " +
            "left join items as i on b.item_id = i.id " +
            "where i.user_id = ?1 " +
            "order by b.end_time desc ", nativeQuery = true)
    List<Booking> findAllByOwnerId(long ownerId);

    @Query(value = "select i.user_id " +
            "from items as i " +
            "left join bookings as b on b.item_id = i.id " +
            "where i.id = b.item_id and b.id = ?1", nativeQuery = true)
    Long findOwnerIdByBookingId(long bookingId);

    @Query(value = "select i.user_id " +
            "from items as i " +
            "where i.id = ?1", nativeQuery = true)
    Long findOwnerIdByItemId(long itemId);

    @Query(value = "select b.* " +
            "from bookings as b " +
            "where b.item_id = ?1 and b.end_time < ?2 " +
            "order by end_time desc " +
            "limit 1", nativeQuery = true)
    Optional<Booking> findLastBookingByItemId(long itemId, LocalDateTime nowTime);

    @Query(value = "select b.* " +
            "from bookings as b " +
            "where b.item_id = ?1 and b.start_time > ?2 " +
            "order by start_time " +
            "limit 1", nativeQuery = true)
    Optional<Booking> findNextBookingByItemId(long itemId, LocalDateTime nowTime);

    @Query(value = "select b.* " +
            "from bookings as b " +
            "where b.booker_id = ?1 and b.item_id = ?2 and b.end_time < ?3 " +
            "order by end_time desc " +
            "limit 1", nativeQuery = true)
    Optional<Booking> findBookingByUserIdAndItemId(long userId, long itemId, LocalDateTime nowTime);
}
