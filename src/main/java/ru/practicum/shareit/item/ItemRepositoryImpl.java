package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    ItemRepository repository;
    List<String> searchHistory = new ArrayList<>();

    public ItemRepositoryImpl(@Lazy ItemRepository repository) {
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
