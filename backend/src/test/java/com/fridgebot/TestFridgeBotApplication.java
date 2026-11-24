package com.fridgebot;

import org.springframework.boot.SpringApplication;

public class TestFridgeBotApplication {

	public static void main(String[] args) {
		SpringApplication.from(FridgeBotApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
