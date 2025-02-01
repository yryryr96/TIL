package com.example.cafekiosk.spring.api.service.product;

import com.example.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import com.example.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import com.example.cafekiosk.spring.domain.product.Product;
import com.example.cafekiosk.spring.domain.product.ProductRepository;
import com.example.cafekiosk.spring.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.cafekiosk.spring.domain.product.ProductType.*;
import static com.example.cafekiosk.spring.domain.product.productSellingStatus.*;

/**
 * readOnly = true : 읽기 전용
 * CRUD 에서 CUD 동작 X / only read
 * JPA : CUD 스냅샷 저장 X, 변경 감지 X (성능 향상)
 * CQRS - Command / Query
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductNumberFactory productNumberFactory;

    @Transactional
    public ProductResponse createProduct(ProductCreateServiceRequest request) {

        // DB에서 마지막 저장된 Product의 상품 번호를 읽어와서 +1
        String nextProductNumber = productNumberFactory.createNextProductNumber();

        Product product = request.toEntity(nextProductNumber);
        Product savedProduct = productRepository.save(product);

        return ProductResponse.of(savedProduct);
    }

    public List<ProductResponse> getSellingProducts() {

        List<Product> products = productRepository.findAllBySellingStatusIn(forDisplay());

        return products.stream()
                .map(ProductResponse::of)
                .toList();
    }
}
