package com.example.cafekiosk.spring.domain.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    @DisplayName("남아있는 재고 수량이 주문 수량보다 작은지 확인한다.")
    @Test
    void isQuantityLessThan() throws Exception {
        //given
        Stock stock = Stock.create("001", 1);
        int quantity = 2;

        //when
        boolean result = stock.isQuantityLessThan(quantity);

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("재고를 주어진 개수만큼 차감할 수 있다.")
    @Test
    void deductQuantity() throws Exception {
        //given
        Stock stock = Stock.create("001", 1);
        int quantity = 1;

        //when
        stock.deductQuantity(quantity);

        //then
        assertThat(stock.getQuantity()).isZero();
    }

    @DisplayName("재고보다 많은 수량으로 차감 시도하는 경우 예외가 발생한다.")
    @Test
    void deductQuantity2() throws Exception {
        //given
        Stock stock = Stock.create("001", 1);
        int quantity = 2;

        //when

        //then
        assertThatThrownBy(() -> stock.deductQuantity(quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("차감할 재고 수량이 없습니다.");
    }

    @DisplayName("재고 차감 시나리오") // 하나의 자원을 연속적으로 사용해야 하는 경우 등, 상태를 공유하는 시나리오 테스트가 필요한 경우 사용
    @TestFactory
    Collection<DynamicTest> stockDeductionDynamicTest() {

        // given
        Stock stock = Stock.create("001", 1);

        return List.of(
                DynamicTest.dynamicTest("재고를 주어진 개수만큼 차감할 수 있다.", () -> {
                    // given
                    int quantity = 1;

                    // when
                    stock.deductQuantity(quantity);

                    // then
                    assertThat(stock.getQuantity()).isZero();
                }),
                DynamicTest.dynamicTest("재고보다 많은 수의 수량으로 차감을 시도하는 경우 예외가 발생한다.", () -> {
                    // given
                    int quantity = 1;

                    // when, then
                    assertThatThrownBy(() -> stock.deductQuantity(quantity))
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessage("차감할 재고 수량이 없습니다.");
                })
        );
    }
}