package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingServiceImpl implements BookingService {
    BookingRepository repository;
    ItemService itemService;
    UserService userService;

    @Transactional
    @Override
    public BookingDtoOut saveBooking(long bookerId, BookingDtoIn bookingDtoIn) {
        userValid(bookerId);
        if (itemService.getItemById(bookerId, bookingDtoIn.getItemId()).getAvailable().equals(false)) {
            log.error("Этой вещи нет в наличии!");
            throw new IncorrectParameterException("Этой вещи нет в наличии!");
        }
        if (repository.findOwnerIdByItemId(bookingDtoIn.getItemId()).equals(bookerId)) {
            log.error("Владелец вещи не может забронировать свою вещь!");
            throw new NotFoundException("Владелец вещи не может забронировать свою вещь!");
        }
        Booking booking = BookingMapper.toBooking(bookerId, bookingDtoIn);
        booking.setStatus(BookingStatus.WAITING);
        repository.save(booking);
        BookingDtoOut bookingDtoOut = setBookerAndItem(booking, userService.getUserById(bookerId),
                itemService.getItemById(bookerId, bookingDtoIn.getItemId()));
        return bookingDtoOut;
    }

    @Override
    public BookingDtoOut getBookingById(long userId, long id) {
        bookingValid(id);
        if (!repository.getById(id).getBookerId().equals(userId) && !repository.findOwnerIdByBookingId(id).equals(userId)) {
            log.error("Пользователь с id {} не имеет доступа к бронированию с id {}!", userId, id);
            throw new NotFoundException("Пользователь не имеет доступа к этому бронированию!");
        }
        BookingDtoOut bookingDtoOut = setBookerAndItem(repository.getById(id),
                userService.getUserById(repository.getById(id).getBookerId()), itemService.getItemById(userId,
                        repository.getById(id).getItemId()));
        return bookingDtoOut;
    }

    @Transactional
    @Override
    public BookingDtoOut updateBooking(long ownerId, String approved, long id) {
        userValid(ownerId);
        bookingValid(id);
        if (!repository.findOwnerIdByBookingId(id).equals(ownerId)) {
            log.error("Пользователь с id {} не имеет доступа к бронированию с id {}!", ownerId, id);
            throw new NotFoundException("Пользователь не имеет доступа к этому бронированию!");
        }
        Booking booking = repository.getById(id);
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            log.error("Поменять статус бронирования с id {} уже нельзя!", id);
            throw new IncorrectParameterException("Поменять статус этого бронирования уже нельзя!");
        }
        if (approved.equals("true")) {
            booking.setStatus(BookingStatus.APPROVED);
        }
        if (approved.equals("false")) {
            booking.setStatus(BookingStatus.REJECTED);
        }
        repository.save(booking);
        BookingDtoOut bookingDtoOut = setBookerAndItem(booking, userService.getUserById(repository.getById(id).getBookerId()),
                itemService.getItemById(ownerId, repository.getById(id).getItemId()));
        return bookingDtoOut;
    }

    @Override
    public List<BookingDtoOut> getAllBookingsByBookerId(long bookerId, String state, long from, long size) {
        userValid(bookerId);
        List<BookingDtoOut> bookingDtoOuts = new ArrayList<>();
        for (Booking booking : repository.findAllByBookerIdOrderByEndDesc(bookerId)) {
            BookingDtoOut bookingDtoOut = setBookerAndItem(booking, userService.getUserById(repository.getById(bookerId).getBookerId()),
                    itemService.getItemById(bookerId, booking.getItemId()));
            addBookingsByState(booking, bookingDtoOuts, bookingDtoOut, state);
        }
        return bookingDtoOuts.stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoOut> getAllBookingsByOwnerId(long ownerId, String state, long from, long size) {
        userValid(ownerId);
        List<BookingDtoOut> bookingDtoOuts = new ArrayList<>();
        for (Booking booking : repository.findAllByOwnerId(ownerId)) {
            BookingDtoOut bookingDtoOut = setBookerAndItem(booking, userService.getUserById(booking.getBookerId()),
                    itemService.getItemById(ownerId, booking.getItemId()));
            addBookingsByState(booking, bookingDtoOuts, bookingDtoOut, state);
        }
        return bookingDtoOuts.stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    private void addBookingsByState(Booking booking, List<BookingDtoOut> bookingDtoOuts, BookingDtoOut bookingDtoOut, String state) {
        switch (state) {
            case "ALL":
                bookingDtoOuts.add(bookingDtoOut);
                break;
            case "CURRENT":
                if (booking.getStart().isBefore(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now())) {
                    bookingDtoOuts.add(bookingDtoOut);
                }
                break;
            case "PAST":
                if (booking.getEnd().isBefore(LocalDateTime.now())) {
                    bookingDtoOuts.add(bookingDtoOut);
                }
                break;
            case "FUTURE":
                if (booking.getStart().isAfter(LocalDateTime.now())) {
                    bookingDtoOuts.add(bookingDtoOut);
                }
                break;
            case "WAITING":
                if (booking.getStatus().equals(BookingStatus.WAITING)) {
                    bookingDtoOuts.add(bookingDtoOut);
                }
                break;
            case "REJECTED":
                if (booking.getStatus().equals(BookingStatus.REJECTED)) {
                    bookingDtoOuts.add(bookingDtoOut);
                }
                break;
            default:
                log.error("Unknown state: {}", state);
                throw new IncorrectParameterException("Unknown state: " + state);
        }
    }

    private BookingDtoOut setBookerAndItem(Booking booking, UserDto userDto, ItemDto itemDto) {
        BookingDtoOut bookingDtoOut = BookingMapper.toBookingDtoOut(booking);
        bookingDtoOut.setBooker(userDto);
        bookingDtoOut.setItem(itemDto);
        return bookingDtoOut;
    }

    private void userValid(long userId) {
        if (!userService.getAllUsers().stream()
                .map(UserDto::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("Пользователя с id не существует! {}", userId);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
    }

    private void bookingValid(long id) {
        if (!repository.findAll().stream()
                .map(Booking::getId)
                .collect(Collectors.toList())
                .contains(id)) {
            log.error("Бронирования с id не существует! {}", id);
            throw new NotFoundException("Бронирования с таким id не существует!");
        }
    }

}
