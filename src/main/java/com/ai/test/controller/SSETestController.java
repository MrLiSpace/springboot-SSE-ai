package com.ai.test.controller;

import com.ai.test.param.Param;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/ai")
@Slf4j
public class SSETestController {
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    // 前端连接的SSE端点
    @RequestMapping(path = "/question", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Param param) {
        SseEmitter emitter = new SseEmitter(120_000L);

        threadPoolTaskExecutor.execute(() -> {
            try {
                // 定义请求的 URL
                URL url = new URL("" + "");
                // 打开 HTTP 连接
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // 设置请求方法为 POST
                connection.setRequestMethod("POST");
                // 设置请求头
                // 允许输出
                connection.setDoOutput(true);
                // 定义请求体
                String requestBody = JSONObject.toJSONString(param);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // 获取响应码
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 读取响应
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = readLineWithEmbeddedNewLines(reader)) != null) {
                            //log.info("event数据:{}", line);
                            line = line.replace("data:", "");
                            // 去除末尾的换行符
                            line = removeTrailingNewline(line);
                            log.info("event数据:{}", line);
                            sendDataToFrontend(emitter, line);
                        }
                    }
                } else {
                    emitter.completeWithError(new Exception("服务繁忙,请稍后再试"));
                }
            } catch (IOException e) {
                emitter.completeWithError(new Exception("服务繁忙,请稍后再试"));
            } finally {
                emitter.complete();
            }
        });

        return emitter;
    }

    private void sendDataToFrontend(SseEmitter emitter, String data) {
        try {
            emitter.send(data, MediaType.TEXT_EVENT_STREAM);
        } catch (IOException e) {
            handleError(emitter);
        }
    }

    private void handleError(SseEmitter emitter) {
        try {
            emitter.send("服务器异常，请稍后再试");
            emitter.complete();
        } catch (IOException e) {
            emitter.complete();
        }
    }

    public static String readLineWithEmbeddedNewLines(BufferedReader reader) throws IOException {
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            line.append((char) c);
            if (c == '\n') {
                if (isEndOfLine(reader)) {
                    break;
                }
            }
        }
        return line.length() == 0 ? null : line.toString();
    }

    private static boolean isEndOfLine(BufferedReader reader) throws IOException {
        reader.mark(1);
        int nextChar = reader.read();
        if (nextChar == -1) {
            return true;
        }
        if (nextChar != '\n') {
            reader.reset();
        }
        return nextChar == '\n';
    }

    private static String removeTrailingNewline(String line) {
        line = line.replace("\n\n", "\ndata:\ndata:");
        if (line.endsWith("\n")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }
}
