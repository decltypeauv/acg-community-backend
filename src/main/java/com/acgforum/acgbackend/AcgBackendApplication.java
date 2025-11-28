package com.acgforum.acgbackend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.acgforum.acgbackend.utils.ConsoleColor;




@SpringBootApplication
public class AcgBackendApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(AcgBackendApplication.class, args);
		ConsoleColor.printSuccess("猿神 启动！");

	}

}
