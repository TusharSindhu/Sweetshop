package com.incubyte.sweetshop.sweet;

import org.springframework.stereotype.Service;

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
}
