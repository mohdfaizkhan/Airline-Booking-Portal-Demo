package com.mohdfai.example.service;

import com.mohdfai.example.model.Flight;
import com.mohdfai.example.model.Itinerary;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mohdfai
 */
@RefreshScope
@Configuration
public class ExposedConfig {

    /**
     * value example
     */
    @Bean
    public Supplier<String> hello() {
        return () -> "hello";
    }

    @Bean
    public Function<Flight, Itinerary> price() {
        return SalesTicketingService::price;
    }

}
