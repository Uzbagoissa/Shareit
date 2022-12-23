package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService{
    private final BookingRepository repository;
    private final ItemService itemService;
    private final UserService userService;
    @Override
    public BookingDtoOut saveBooking(long bookerId, BookingDtoIn bookingDtoIn) {
        userValid(bookerId);
        if (itemService.getItemById(bookingDtoIn.getItemId()).getAvailable().equals(false)) {
            log.error("Этой вещи нет в наличии!");
            throw new IncorrectParameterException("Этой вещи нет в наличии!");
        }
        if (bookingDtoIn.getEnd().isBefore(LocalDateTime.now())) {
            log.error("Некорректное время окончания аренды!");
            throw new IncorrectParameterException("Некорректное время окончания аренды!");
        }
        if (bookingDtoIn.getEnd().isBefore(bookingDtoIn.getStart())) {
            log.error("Время окончания аренды раньше времени начала аренды!");
            throw new IncorrectParameterException("Время окончания аренды раньше времени начала аренды!");
        }
        if (bookingDtoIn.getStart().isBefore(LocalDateTime.now())) {
            log.error("Некорректное время начала аренды!");
            throw new IncorrectParameterException("Некорректное время начала аренды!");
        }
        Booking booking = BookingMapper.toBooking(bookerId, bookingDtoIn);
        booking.setStatus(BookingStatus.WAITING);
        repository.save(booking);
        BookingDtoOut bookingDtoOut = BookingMapper.toBookingDtoOut(booking);
        bookingDtoOut.setBooker(userService.getUserById(bookerId));
        bookingDtoOut.setItem(itemService.getItemById(bookingDtoIn.getItemId()));
        return bookingDtoOut;
    }

    @Override
    public BookingDtoOut getBookingById(long id) {
        bookingValid(id);
        BookingDtoOut bookingDtoOut = BookingMapper.toBookingDtoOut(repository.getById(id));
        bookingDtoOut.setBooker(userService.getUserById(repository.getById(id).getBookerId()));
        bookingDtoOut.setItem(itemService.getItemById(repository.getById(id).getItemId()));
        return bookingDtoOut;
    }

    @Override
    public BookingDtoOut updateBooking(long ownerId, String approved, long id) {
        userValid(ownerId);
        bookingValid(id);
        Booking booking = repository.getById(id);
        if (approved.equals("true")){
            booking.setStatus(BookingStatus.APPROVED);
        }
        if (approved.equals("false")){
            booking.setStatus(BookingStatus.REJECTED);
        }
        repository.save(booking);
        BookingDtoOut bookingDtoOut = BookingMapper.toBookingDtoOut(booking);
        bookingDtoOut.setBooker(userService.getUserById(repository.getById(id).getBookerId()));
        bookingDtoOut.setItem(itemService.getItemById(repository.getById(id).getItemId()));
        return bookingDtoOut;
    }

    @Override
    public List<BookingDtoOut> getAllBookingsByBookerId(long bookerId, String state) {
        userValid(bookerId);
        List<BookingDtoOut> bookingDtoOuts = new ArrayList<>();
        for (Booking booking : repository.findAllByBookerIdOrderByEndDesc(bookerId)) {
            BookingDtoOut bookingDtoOut = BookingMapper.toBookingDtoOut(booking);
            bookingDtoOut.setBooker(userService.getUserById(repository.getById(bookerId).getBookerId()));
            bookingDtoOut.setItem(itemService.getItemById(booking.getItemId()));
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
        return bookingDtoOuts;
    }

    @Override
    public List<BookingDtoOut> getAllBookingsByOwnerId(long ownerId, String state) {
        userValid(ownerId);
        switch (state) {
            case "ALL":
                return  repository.findAllByOwnerId(ownerId).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            case "CURRENT":
                return  repository.findAllByOwnerId(ownerId).stream()
                        .filter(a -> a.getStart().isBefore(LocalDateTime.now()) && a.getEnd().isAfter(LocalDateTime.now()))
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            case "PAST":
                return  repository.findAllByOwnerId(ownerId).stream()
                        .filter(a -> a.getEnd().isBefore(LocalDateTime.now()))
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            case "FUTURE":
                return  repository.findAllByOwnerId(ownerId).stream()
                        .filter(a -> a.getStart().isAfter(LocalDateTime.now()))
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            case "WAITING":
                return  repository.findAllByOwnerId(ownerId).stream()
                        .filter(a -> a.getStatus().equals(BookingStatus.WAITING))
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            case "REJECTED":
                return  repository.findAllByOwnerId(ownerId).stream()
                        .filter(a -> a.getStatus().equals(BookingStatus.REJECTED))
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            default:
                log.error("Unknown state: {}", state);
                throw new IncorrectParameterException("Unknown state: " + state);
        }
    }

    private void userValid(long userId) {
        if (!userService.getAllUsers().stream()
                .map(UserDto::getId)
                .collect(Collectors.toList())
                .contains(userId)) {
            log.error("Пользователя с таким id не существует! {}", userId);
            throw new NotFoundException("Пользователя с таким id не существует!");
        }
    }

    private void bookingValid(long id) {
        if (!repository.findAll().stream()
                .map(Booking::getId)
                .collect(Collectors.toList())
                .contains(id)) {
            log.error("Бронирования с таким id не существует! {}", id);
            throw new NotFoundException("Бронирования с таким id не существует!");
        }
    }

}
