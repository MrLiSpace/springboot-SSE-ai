package com.ai.test.param;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Param implements Serializable {
    private static final long serialVersionUID = 827783162708877374L;
    private String chatId;
    private List<Message> messages;

    @Data
    public static class Message implements Serializable{

        private static final long serialVersionUID = 8109451235991421215L;

        private String role;

        private String content;
    }
}
