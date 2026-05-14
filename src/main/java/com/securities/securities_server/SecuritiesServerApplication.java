package com.securities.securities_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SecuritiesServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecuritiesServerApplication.class, args);
	}

}
