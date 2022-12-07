package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.user.model.User;

import java.util.Date;

public class ItemRequestDto {
    private long id;
    private String description;
    private User requestor;
    private Date created;
}
