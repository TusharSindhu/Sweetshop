package com.incubyte.sweetshop;

import org.springframework.boot.SpringApplication;

public class TestSweetshopApplication {

	public static void main(String[] args) {
		SpringApplication.from(SweetshopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
