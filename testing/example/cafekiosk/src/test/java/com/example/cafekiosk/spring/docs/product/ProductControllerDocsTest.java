package com.example.cafekiosk.spring.docs.product;

import com.example.cafekiosk.spring.api.controller.product.ProductController;
import com.example.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import com.example.cafekiosk.spring.api.service.product.ProductService;
import com.example.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import com.example.cafekiosk.spring.docs.RestDocsSupport;
import com.example.cafekiosk.spring.dto.response.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.example.cafekiosk.spring.domain.product.ProductType.HANDMADE;
import static com.example.cafekiosk.spring.domain.product.productSellingStatus.SELLING;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerDocsTest extends RestDocsSupport {

    private final ProductService productService = mock(ProductService.class);

    @Override
    protected Object initController() {
        return new ProductController(productService);
    }

    @DisplayName("신규 상품을 등록하는 API")
    @Test
    void createProduct() throws Exception {

        //given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .type(HANDMADE)
                .sellingStatus(SELLING)
                .name("아메리카노")
                .price(4000)
                .build();

        given(productService.createProduct(any(ProductCreateServiceRequest.class)))
                .willReturn(ProductResponse.builder()
                        .id(1L)
                        .productNumber("001")
                        .type(HANDMADE)
                        .sellingStatus(SELLING)
                        .name("아메리카노")
                        .price(4000)
                        .build());

        //when
        //then
        mockMvc.perform(post("/api/v1/products/new")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("product-create",
                        preprocessRequest(prettyPrint()), // adoc에서 json 형태를 보기 좋게 만들어줌
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("type").type(JsonFieldType.STRING)
                                        .description("상품 타입"),
                                fieldWithPath("sellingStatus").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("상품 판매 상태"),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("상품명"),
                                fieldWithPath("price").type(JsonFieldType.NUMBER)
                                        .description("상품 가격")
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
                                        .description("상품 ID"),
                                fieldWithPath("data.productNumber").type(JsonFieldType.STRING)
                                        .description("상품 번호"),
                                fieldWithPath("data.type").type(JsonFieldType.STRING)
                                        .description("상품 타입"),
                                fieldWithPath("data.sellingStatus").type(JsonFieldType.STRING)
                                        .description("상품 판매 상태"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING)
                                        .description("상품명"),
                                fieldWithPath("data.price").type(JsonFieldType.NUMBER)
                                        .description("상품 가격")
                        ))
                );

    }

    @DisplayName("판매 상태의 상품 조회")
    @Test
    void getSellingProducts() throws Exception {

        //given
        List<ProductResponse> productResponses = List.of(
                ProductResponse.builder()
                        .id(1L)
                        .productNumber("001")
                        .type(HANDMADE)
                        .sellingStatus(SELLING)
                        .name("아메리카노")
                        .price(4000)
                        .build(),
                ProductResponse.builder()
                        .id(2L)
                        .productNumber("002")
                        .type(HANDMADE)
                        .sellingStatus(SELLING)
                        .name("카푸치노")
                        .price(5000)
                        .build()
        );

        given(productService.getSellingProducts())
                .willReturn(productResponses);

        //when
        //then
        mockMvc.perform(get("/api/v1/products/selling"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-selling-products",
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY)
                                        .description("응답 데이터"),
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
                                        .description("상품 ID"),
                                fieldWithPath("data[].productNumber").type(JsonFieldType.STRING)
                                        .description("상품 번호"),
                                fieldWithPath("data[].type").type(JsonFieldType.STRING)
                                        .description("상품 타입"),
                                fieldWithPath("data[].sellingStatus").type(JsonFieldType.STRING)
                                        .description("상품 판매 상태"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING)
                                        .description("상품명"),
                                fieldWithPath("data[].price").type(JsonFieldType.NUMBER)
                                        .description("상품 가격")
                        ))
                );
    }
}
