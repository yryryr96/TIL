package com.example.cafekiosk.spring.domain.product;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public enum productSellingStatus {
    
    SELLING("판매중"),
    HOLD("판매보류"),
    STOP_SELLING("판매중지");
    
    private final String text;

    public static List<productSellingStatus> forDisplay() {
        return List.of(SELLING, HOLD);
    }
}
