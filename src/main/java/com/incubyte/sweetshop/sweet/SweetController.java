package com.incubyte.sweetshop.sweet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @PostMapping("/{id}/purchase")
    public ResponseEntity<Void> purchaseSweet(@PathVariable Long id) {
        sweetService.purchaseSweet(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<SweetResponse>> searchSweets(@RequestParam(required = false) String name) {
        List<Sweet> sweets = sweetService.searchSweets(name);

        List<SweetResponse> response = sweets.stream()
                .map(sweet -> new SweetResponse(
                        sweet.getId(), sweet.getName(), sweet.getCategory(), sweet.getPrice(), sweet.getQuantity()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSweet(@PathVariable Long id) {
        sweetService.deleteSweet(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restockSweet(@PathVariable Long id, @RequestBody RestockRequest request) {
        sweetService.restockSweet(id, request.quantity());
        return ResponseEntity.ok().build();
    }
}
