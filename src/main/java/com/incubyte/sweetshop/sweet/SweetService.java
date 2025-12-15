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
}
