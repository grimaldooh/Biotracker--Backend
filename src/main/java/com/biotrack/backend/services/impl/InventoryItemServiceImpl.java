package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.InventoryItem;
import com.biotrack.backend.repositories.InventoryItemRepository;
import com.biotrack.backend.services.InventoryItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository repository;

    public InventoryItemServiceImpl(InventoryItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public InventoryItem create(InventoryItem item) {
        return repository.save(item);
    }

    @Override
    public List<InventoryItem> findAll() {
        return repository.findAll();
    }

    @Override
    public InventoryItem findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public InventoryItem update(UUID id, InventoryItem updatedItem) {
        InventoryItem existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory item not found"));
        // existing.setName(updatedItem.getName());
        // existing.setDescription(updatedItem.getDescription());
        existing.setQuantity(updatedItem.getQuantity());
        // existing.setLocation(updatedItem.getLocation());
        // existing.setCategory(updatedItem.getCategory());
        return repository.save(existing);
    }
}