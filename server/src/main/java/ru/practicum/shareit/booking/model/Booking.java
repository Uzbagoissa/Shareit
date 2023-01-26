package ru.practicum.shareit.booking.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "bookings", schema = "public")
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    public static final String bookingTable = "bookingTable";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "start_time", nullable = false)
    LocalDateTime start;

    @Column(name = "end_time", nullable = false)
    LocalDateTime end;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    BookingStatus status;

    @Column(name = "booker_id", nullable = false)
    Long bookerId;

    @Column(name = "item_id", nullable = false)
    Long itemId;

    @Transient
    User user;

    @Transient
    Item item;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id) && Objects.equals(start, booking.start) && Objects.equals(end, booking.end) && status == booking.status && Objects.equals(bookerId, booking.bookerId) && Objects.equals(itemId, booking.itemId) && Objects.equals(user, booking.user) && Objects.equals(item, booking.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start, end, status, bookerId, itemId, user, item);
    }
}
