package com.taskhttpv1.demo.TaskTrackerPro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskTrackerProApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskTrackerProApplication.class, args);
	}
}
