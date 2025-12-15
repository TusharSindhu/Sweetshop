package com.incubyte.sweetshop.sweet;

import org.springframework.stereotype.Service;
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

    public List<Sweet> searchSweets(String name) {
        if (name != null && !name.isBlank()) {
            return sweetRepository.findByNameContainingIgnoreCase(name);
        }
        return sweetRepository.findAll();
    }

    public void deleteSweet(Long id) {
        if (!sweetRepository.existsById(id)) {
            throw new RuntimeException("Sweet not found");
        }
        sweetRepository.deleteById(id);
    }
}
