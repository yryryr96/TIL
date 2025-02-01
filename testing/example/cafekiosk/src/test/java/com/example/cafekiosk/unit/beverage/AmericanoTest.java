package com.example.cafekiosk.unit.beverage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class AmericanoTest {

    @Test
    void getName() throws Exception {
        //given
        Americano americano = new Americano();
        //when

        //then
        assertThat(americano.getName()).isEqualTo("아메리카노");
    }

    @Test
    void getPrice() throws Exception {
        //given
        Americano americano = new Americano();
        //when

        //then
        assertThat(americano.getPrice()).isEqualTo(4000);
    }
}