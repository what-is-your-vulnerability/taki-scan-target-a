package com.bss.customer;

import java.util.TimeZone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.bss.customer", annotationClass = org.apache.ibatis.annotations.Mapper.class)
public class CustomerServiceApplication {

    public static void main(String[] args) {
        // DB 시각 컬럼은 한국 표준시(KST) 기준으로 다룬다.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
