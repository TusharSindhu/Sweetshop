package com.incubyte.sweetshop.sweet;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SweetService {
    private final SweetRepository sweetRepository;

    public SweetService(SweetRepository sweetRepository) {
        this.sweetRepository = sweetRepository;
    }

    public Sweet addSweet(SweetRequest request) {
        Sweet sweet = new Sweet(request.name(), request.category(), request.price(), request.quantity());
        return sweetRepository.save(sweet);
    }

    public List<Sweet> getAllSweets() {
        return sweetRepository.findAll();
    }

    public void purchaseSweet(Long id) {
        Sweet sweet = sweetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sweet not found"));

        if (sweet.getQuantity() <= 0) {
            throw new RuntimeException("Sweet is out of stock");
        }

        sweet.setQuantity(sweet.getQuantity() - 1);
        sweetRepository.save(sweet);
    }

    public List<Sweet> searchSweets(String name, String category, BigDecimal maxPrice) {
        return sweetRepository.searchSweets(name, category, maxPrice);
    }

    public void deleteSweet(Long id) {
        if (!sweetRepository.existsById(id)) {
            throw new RuntimeException("Sweet not found");
        }
        sweetRepository.deleteById(id);
    }

    public void restockSweet(Long id, Integer amountToAdd) {
        Sweet sweet = sweetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sweet not found"));

        if (amountToAdd == null || amountToAdd <= 0) {
            throw new RuntimeException("Restock amount must be positive");
        }

        sweet.setQuantity(sweet.getQuantity() + amountToAdd);
        sweetRepository.save(sweet);
    }

    public void updateSweet(Long id, SweetRequest request) {
        Sweet sweet = sweetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sweet not found"));

        sweet.setName(request.name());
        sweet.setCategory(request.category());
        sweet.setPrice(request.price());
        // Quantity is handled by restock/purchase, but admin can override here if needed
        sweet.setQuantity(request.quantity());

        sweetRepository.save(sweet);
    }
}
