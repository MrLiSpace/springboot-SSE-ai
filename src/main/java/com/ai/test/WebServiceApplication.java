package com.ai.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author tengfeiwang
 */
@SpringBootApplication(scanBasePackages = {"com.ai.test"})
public class WebServiceApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(WebServiceApplication.class, args);
        Environment environment = configurableApplicationContext.getBean(Environment.class);
        System.out.println("\n\n ============> 系统启动成功！后台地址：http://localhost:" + environment.getProperty("server.port") + "\n");
        // 打印 swagger 文档地址
        System.out.println("项目启动启动成功！swagger 接口文档地址: http://localhost:" + environment.getProperty("server.port") + "/swagger-ui.html");
    }
}
