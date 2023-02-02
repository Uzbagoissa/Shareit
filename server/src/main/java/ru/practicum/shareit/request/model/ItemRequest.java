package ru.practicum.shareit.request.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "requests", schema = "public")
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequest {
    public static final String requestsTable = "requestsTable";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "requester_id", nullable = false)
    Long requesterId;

    @Column(name = "created", nullable = false)
    LocalDateTime created;

    @Transient
    User requester;

    @Transient
    List<ItemDto> items;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemRequest that = (ItemRequest) o;
        return Objects.equals(id, that.id) && Objects.equals(description, that.description) && Objects.equals(requesterId, that.requesterId) && Objects.equals(created, that.created) && Objects.equals(requester, that.requester) && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, requesterId, created, requester, items);
    }
}
