package com.ajua.Dromed;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DromedApplication {
	public static void main(String[] args) {
		SpringApplication.run(DromedApplication.class, args);
		System.out.println("Building a Drone Dispatch System for Medication Transport");
	}

}
