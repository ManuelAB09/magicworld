package com.magicworld.tfg_angular_springboot;

import com.magicworld.tfg_angular_springboot.util.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MagicWorldApplication {

	public static void main(String[] args) {
		DotenvLoader.loadIfExists();
		SpringApplication.run(MagicWorldApplication.class, args);
	}

}
