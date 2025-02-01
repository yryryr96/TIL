### 한 문단에 한 주제만 작성하자.

- 논리구조 (반복문, 분기문)이 들어간 테스트 자체가 두 가지 이상의 주제가 들어가 있다는 반증이다.

- 테스트를 읽는 사람이 논리구조에 따라 한 번 더 생각하게 된다.
- 꼭 필요한 경우 `@ParameterizedTest`와 같은 다른 방법을 사용하자.

##### 여러 주제가 포함된 테스트

```java
@Test
void containsStockTypeEx() {
    //given
    ProductType[] productTypes = ProductType.values();
    
    for (ProductType productType : productTypes) {
        if (productType == ProductType.HANDMADE) {
            //when
            boolean result = ProductType.containsStockType(productType);
            
            //then
            assertThat(result).isFalse();
        }
        
        if (productType == ProductType.BAKERY || productType == ProductType.BOTTLE) {
            //when
            boolean result = ProductType.containsStockType(productType);
            
            //then
            assertThat(result).isTrue();
        }
    }
}
```

##### 한개의 주제만 포함된 테스트

```java
@DisplayName("상품 타입이 재고 관련 타입인지를 확인한다.")
@Test
void containsStockType1() throws Exception {
    //given
    ProductType givenType = ProductType.HANDMADE;

    //when
    boolean result = ProductType.containsStockType(givenType);

    //then
    assertThat(result).isFalse();
}

@DisplayName("상품 타입이 재고 관련 타입인지를 확인한다.")
@Test
void containsStockType2() throws Exception {
    //given
    ProductType givenType = ProductType.BOTTLE;

    //when
    boolean result = ProductType.containsStockType(givenType);

    //then
    assertThat(result).isTrue();
}

@DisplayName("상품 타입이 재고 관련 타입인지를 확인한다.")
@Test
void containsStockType3() throws Exception {
    //given
    ProductType givenType = ProductType.BAKERY;

    //when
    boolean result = ProductType.containsStockType(givenType);

    //then
    assertThat(result).isTrue();
}
```

한 개의 테스트에서 if, for문을 사용하지 말고 위와 같이 한 개의 주제로 여러번 테스트를 작성하자.