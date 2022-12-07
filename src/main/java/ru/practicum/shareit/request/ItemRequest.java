package ru.practicum.shareit.request;

import ru.practicum.shareit.user.model.User;

import java.util.Date;

public class ItemRequest {
    private long id;
    private String description;
    private User requestor;
    private Date created;
}
