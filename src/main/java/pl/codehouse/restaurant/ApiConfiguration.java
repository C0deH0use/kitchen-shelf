package pl.codehouse.restaurant;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.time.Clock;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration class for base API settings.
 * This class provides beans for clock, message source, and validator factory.
 */
@Configuration
public class ApiConfiguration {

    private static final String MESSAGES_BASENAME = "classpath:messages";

    /**
     * Provides a Clock bean for the application.
     *
     * @return A Clock instance set to UTC time zone.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Configures and provides a MessageSource bean for internationalization.
     *
     * @return A configured ResourceBundleMessageSource instance.
     */
    @Bean
    @Qualifier("parentMessageSource")
    public MessageSource parentMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        messageSource.setBasename(MESSAGES_BASENAME);
        messageSource.setDefaultEncoding(UTF_8.displayName());
        messageSource.setDefaultLocale(Locale.ENGLISH);
        return messageSource;
    }

    /**
     * Configures and provides a LocalValidatorFactoryBean for validation.
     *
     * @param messageSource The MessageSource to be used for validation messages.
     * @return A configured LocalValidatorFactoryBean instance.
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}
