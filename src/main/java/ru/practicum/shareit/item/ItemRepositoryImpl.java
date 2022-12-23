package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ItemRepositoryImpl  implements ItemRepositoryCustom {

    private final ItemRepository repository;
    private final List<String> searchHistory = new ArrayList<>();

    public ItemRepositoryImpl(@Lazy ItemRepository repository){
        this.repository = repository;
    }

    @Override
    public List<Item> searchItems(String text) {
        List<Item> itemsSearched = new ArrayList<>();
        if (text.isBlank()) {
            for (Item item : repository.findAll()) {
                if (item.getName().toLowerCase().contains(searchHistory.get(searchHistory.size() - 1).toLowerCase()) && item.getAvailable().equals(true) ||
                        item.getDescription().toLowerCase().contains(searchHistory.get(searchHistory.size() - 1).toLowerCase()) && item.getAvailable().equals(true)) {
                    return itemsSearched;
                }
            }
        } else {
            for (Item item : repository.findAll()) {
                if (item.getName().toLowerCase().contains(text.toLowerCase()) && item.getAvailable().equals(true) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()) && item.getAvailable().equals(true)) {
                    itemsSearched.add(item);
                    searchHistory.add(text);
                }
            }
        }
        return itemsSearched;
    }
}
