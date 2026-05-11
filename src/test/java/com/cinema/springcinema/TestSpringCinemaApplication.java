package com.cinema.springcinema;

import org.springframework.boot.SpringApplication;

public class TestSpringCinemaApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringCinemaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
