package com.example.cafekiosk.unit.order;

import com.example.cafekiosk.unit.beverage.Beverage;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class Order {

    private final LocalDateTime orderDateTime;
    private final List<Beverage> beverages;
}
