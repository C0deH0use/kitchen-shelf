package pl.codehouse.restaurant;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for base API settings.
 */
@Configuration
public class ApiConfiguration {

    /**
     * Provides a Clock bean for the application.
     *
     * @return A Clock instance set to UTC time zone.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
