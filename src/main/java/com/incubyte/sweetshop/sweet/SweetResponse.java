package com.incubyte.sweetshop.sweet;

import java.math.BigDecimal;

public record SweetResponse(Long id, String name, String category, BigDecimal price, Integer quantity) {}
