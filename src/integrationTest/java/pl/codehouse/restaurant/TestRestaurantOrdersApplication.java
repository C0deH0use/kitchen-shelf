package pl.codehouse.restaurant;

import org.springframework.boot.SpringApplication;

public class TestRestaurantOrdersApplication {

    public static void main(String[] args) {
        SpringApplication.from(KitchenShelfApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }

}
