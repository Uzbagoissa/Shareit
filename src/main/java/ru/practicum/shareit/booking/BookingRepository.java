package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdOrderByEndDesc(long bookerId);

    @Query(value = "select b.* "+
            "from bookings as b " +
            "left join items as i on b.item_id = i.id "+
            "where i.user_id like ?1 " +
            "order by b.end_time desc ", nativeQuery = true)
    List<Booking> findAllByOwnerId(long ownerId);
}
