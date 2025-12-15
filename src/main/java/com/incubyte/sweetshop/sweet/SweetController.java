package com.incubyte.sweetshop.sweet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sweets")
public class SweetController {

    private final SweetService sweetService;

    public SweetController(SweetService sweetService) {
        this.sweetService = sweetService;
    }

    @PostMapping
    public ResponseEntity<SweetResponse> addSweet(@RequestBody SweetRequest request) {
        Sweet savedSweet = sweetService.addSweet(request);
        SweetResponse response = new SweetResponse(
                savedSweet.getId(),
                savedSweet.getName(),
                savedSweet.getCategory(),
                savedSweet.getPrice(),
                savedSweet.getQuantity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<SweetResponse>> getAllSweets() {
        List<Sweet> sweets = sweetService.getAllSweets();

        // Map Entity -> DTO
        List<SweetResponse> response = sweets.stream()
                .map(sweet -> new SweetResponse(
                        sweet.getId(),
                        sweet.getName(),
                        sweet.getCategory(),
                        sweet.getPrice(),
                        sweet.getQuantity()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
