package com.example.cafekiosk.spring.api.service.product;

import com.example.cafekiosk.spring.IntegrationTestSupport;
import com.example.cafekiosk.spring.domain.product.Product;
import com.example.cafekiosk.spring.domain.product.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.cafekiosk.spring.domain.product.ProductType.HANDMADE;
import static com.example.cafekiosk.spring.domain.product.productSellingStatus.SELLING;
import static org.assertj.core.api.Assertions.assertThat;

class ProductNumberFactoryTest extends IntegrationTestSupport {

    @Autowired
    private ProductNumberFactory productNumberFactory;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAllInBatch();
    }

    @DisplayName("최초 상품 등록시 상품 번호는 001 이다.")
    @Test
    void createNextProductNumberInit() throws Exception {
        //given
        //when
        String nextProductNumber = productNumberFactory.createNextProductNumber();

        //then
        assertThat(nextProductNumber).isEqualTo("001");
    }

    @DisplayName("상품 번호는 가장 마지막에 저장된 상품 번호 +1 이다.")
    @Test
    void createNextProductNumber() throws Exception {
        //given
        Product product = createProduct("001");
        productRepository.save(product);

        //when
        String nextProductNumber = productNumberFactory.createNextProductNumber();

        //then
        assertThat(nextProductNumber).isEqualTo("002");
    }

    private Product createProduct(String productNumber) {
        return Product.builder()
                .productNumber(productNumber)
                .type(HANDMADE)
                .sellingStatus(SELLING)
                .price(1000)
                .name("상품명")
                .build();
    }
}