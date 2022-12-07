package ru.practicum.shareit.booking;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Date;

public class Booking {
    private long id;
    private Date start;
    private Date end;
    private Item item;
    private User booker;
    private String status;
}
