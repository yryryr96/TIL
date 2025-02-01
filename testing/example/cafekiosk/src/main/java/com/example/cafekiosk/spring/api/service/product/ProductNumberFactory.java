package com.example.cafekiosk.spring.api.service.product;

import com.example.cafekiosk.spring.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductNumberFactory {

    private final ProductRepository productRepository;

     public String createNextProductNumber() {

        String latestNumber = productRepository.findLatestProductNumber();

        if (latestNumber == null) {
            return "001";
        }

        int latestProductNumberInt = Integer.parseInt(latestNumber);
        int nextProductNumberInt = latestProductNumberInt + 1;

        return String.format("%03d", nextProductNumberInt);
    }
}
