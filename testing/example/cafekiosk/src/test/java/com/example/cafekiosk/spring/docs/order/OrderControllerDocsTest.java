package com.example.cafekiosk.spring.docs.order;

import com.example.cafekiosk.spring.api.controller.order.OrderController;
import com.example.cafekiosk.spring.api.controller.order.dto.request.OrderCreateRequest;
import com.example.cafekiosk.spring.api.service.order.OrderService;
import com.example.cafekiosk.spring.api.service.order.request.OrderCreateServiceRequest;
import com.example.cafekiosk.spring.api.service.order.response.OrderResponse;
import com.example.cafekiosk.spring.docs.RestDocsSupport;
import com.example.cafekiosk.spring.dto.response.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;


import java.time.LocalDateTime;
import java.util.List;

import static com.example.cafekiosk.spring.domain.product.ProductType.HANDMADE;
import static com.example.cafekiosk.spring.domain.product.productSellingStatus.SELLING;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerDocsTest extends RestDocsSupport {

    private final OrderService orderService = mock(OrderService.class);
    private static List<ProductResponse> productResponses;

    @Override
    protected Object initController() {
        return new OrderController(orderService);
    }

    @BeforeEach
    void init() throws Exception {
        ProductResponse productResponse1 = ProductResponse.builder()
                .id(1L)
                .price(1000)
                .sellingStatus(SELLING)
                .productNumber("001")
                .type(HANDMADE)
                .name("상품명1")
                .build();

        ProductResponse productResponse2 = ProductResponse.builder()
                .id(2L)
                .price(3000)
                .sellingStatus(SELLING)
                .productNumber("002")
                .type(HANDMADE)
                .name("상품명2")
                .build();

        productResponses = List.of(productResponse1, productResponse2);
    }

    @DisplayName("새로운 주문을 생성한다.")
    @Test
    void createOrder() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.of(2025, 1, 28, 0, 50);
        List<String> productNumbers = List.of("001", "002");
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(productNumbers)
                .build();
        int totalPrice = productResponses.stream().mapToInt(ProductResponse::getPrice).sum();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(1L)
                .registeredDateTime(now)
                .products(productResponses)
                .totalPrice(totalPrice)
                .build();

        given(orderService.createOrder(any(OrderCreateServiceRequest.class), any(LocalDateTime.class)))
                .willReturn(orderResponse);
        //when
        mockMvc.perform(post("/api/v1/orders/new")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("order-create",
                        preprocessRequest(prettyPrint()), // adoc에서 json 형태를 보기 좋게 만들어줌
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("productNumbers").type(JsonFieldType.ARRAY)
                                        .description("상품 리스트")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                        .description("주문 ID"),
                                fieldWithPath("data.totalPrice").type(JsonFieldType.NUMBER)
                                        .description("총 주문 가격"),
                                fieldWithPath("data.registeredDateTime").type(JsonFieldType.ARRAY) // 반환타입 ARRAY ?
                                        .description("주문 시간"),
                                fieldWithPath("data.products").type(JsonFieldType.ARRAY)
                                        .description("주문 상품 리스트"),
                                fieldWithPath("data.products[].id").type(JsonFieldType.NUMBER)
                                        .description("주문 상품 ID"),
                                fieldWithPath("data.products[].productNumber").type(JsonFieldType.STRING)
                                        .description("주문 상품 번호"),
                                fieldWithPath("data.products[].type").type(JsonFieldType.STRING)
                                        .description("주문 상품 타입"),
                                fieldWithPath("data.products[].sellingStatus").type(JsonFieldType.STRING)
                                        .description("주문 상품 상태"),
                                fieldWithPath("data.products[].name").type(JsonFieldType.STRING)
                                        .description("주문 상품명"),
                                fieldWithPath("data.products[].price").type(JsonFieldType.NUMBER)
                                        .description("주문 상품 가격")
                        ))
                );
    }
}
