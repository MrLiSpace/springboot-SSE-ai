package com.ai.test.controller;

import com.ai.test.param.Param;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/ai/sse/test")
@Slf4j
public class SSETestController {


    // 前端连接的SSE端点
    @PostMapping(value = "/forward", produces = "text/event-stream;charset=UTF-8")
    public Flux<ServerSentEvent<String>> forwardEvents(@RequestBody Param param, HttpServletResponse response) {
        WebClient webClient = WebClient.builder()
                .baseUrl("") // 服务端B地址
                .build();

        // 连接服务端B的SSE
        Flux<ServerSentEvent<String>> eventFlux = webClient.post()
                .uri("/api/v1/open/chat/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(JSONObject.toJSONString(param)))
                .exchangeToFlux(clientResponse -> {
                    HttpStatus httpStatus = clientResponse.statusCode();
                    if (httpStatus.is2xxSuccessful()) {
                        Map<String, ResponseCookie> cookiesFromB =
                                clientResponse.cookies().toSingleValueMap();

                        cookiesFromB.values().forEach(c->{
                            Cookie cookie = new Cookie(c.getName(),c.getValue());
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        });

                        return clientResponse.bodyToFlux(
                                new ParameterizedTypeReference<ServerSentEvent<String>>() {
                                });

                    } else {
                        return Flux.error(new RuntimeException("响应状态异常: " + httpStatus));
                    }

                }).doOnError(c -> log.error("连接集团sse失败", c));
        return eventFlux;
    }
}
