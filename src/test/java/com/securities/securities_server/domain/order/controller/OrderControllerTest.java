package com.securities.securities_server.domain.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securities.securities_server.domain.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.domain.order.controller.response.CancelOrderResponse;
import com.securities.securities_server.domain.order.controller.response.PlaceOrderResponse;
import com.securities.securities_server.domain.order.service.OrderService;
import com.securities.securities_server.global.auth.JwtProvider;
import com.securities.securities_server.global.config.WebConfig;
import com.securities.securities_server.support.TestWebConfig;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.securities.securities_server.domain.order.entity.MatchResult.CANCELLED;
import static com.securities.securities_server.domain.order.entity.MatchResult.MATCHED;
import static com.securities.securities_server.domain.order.entity.OrderSide.BUY;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebConfig.class
        )
)
@Import(TestWebConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    OrderService orderService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 주문_요청_시_주문_응답을_반환한다() throws Exception {
        // given
        PlaceOrderRequest request = new PlaceOrderRequest(1L, BUY, 10000L, 10L);
        PlaceOrderResponse response = new PlaceOrderResponse(1L, MATCHED);
        given(orderService.placeOrder(1L, request)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/order")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(response.orderId()))
                .andExpect(jsonPath("$.matchResult").value("MATCHED"));
    }

    @Test
    void 주문_취소_요청_시_주문_취소_응답을_반환한다() throws Exception {
        // given
        Long userId = 1L;
        Long orderId = 1L;
        CancelOrderResponse response = new CancelOrderResponse(orderId, CANCELLED);
        given(orderService.cancelOrder(userId, orderId)).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/v1/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(response.orderId()))
                .andExpect(jsonPath("$.matchResult").value("CANCELLED"));
    }
}