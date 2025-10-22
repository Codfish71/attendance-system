package com.geeksmetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.geeksmetrics")
public class AttendanceMgmtApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendanceMgmtApplication.class, args);
	}

}
