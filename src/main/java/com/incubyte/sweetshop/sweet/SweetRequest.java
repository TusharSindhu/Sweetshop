package com.incubyte.sweetshop.sweet;

import java.math.BigDecimal;

public record SweetRequest(String name, String category, BigDecimal price, Integer quantity) {}
