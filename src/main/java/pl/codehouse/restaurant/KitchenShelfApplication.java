package pl.codehouse.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Kitchen Shelf Spring Boot Starter class.
 */
@EnableCaching
@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class KitchenShelfApplication {

	public static void main(String[] args) {
		SpringApplication.run(KitchenShelfApplication.class, args);
	}

}
