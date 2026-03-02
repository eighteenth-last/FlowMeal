package com.flowmeal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FlowMeal 智慧订餐外卖平台 - 启动类
 */
@SpringBootApplication
@MapperScan("com.flowmeal.**.mapper")
public class FlowMealApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowMealApplication.class, args);
        System.out.println("  _____ _                __  __            _ \n" +
                " |  ___| | _____      __  \\ \\/ /   ___  __ _| |\n" +
                " | |_  | |/ _ \\ \\ /\\ / /   \\  /  / _ \\/ _` | |\n" +
                " |  _| | | (_) \\ V  V /    /  \\ |  __/ (_| | |\n" +
                " |_|   |_|\\___/ \\_/\\_/    /_/\\_(_)\\___\\__,_|_|\n" +
                "                                                 \n" +
                " 智慧订餐外卖平台 启动成功 \uD83C\uDF54\n" +
                " API文档: http://localhost:8080/api/doc.html\n");
    }
}
