package com.mohdfai.example.service;

import com.mohdfai.example.model.Airport;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

/**
 * @author mohdfai
 */
@RefreshScope
@Configuration
public class ExposedConfig {

    private static Logger logger = Logger.getLogger(Controller.class.getName());

    /**
     * value example
     */
    @Bean
    public Supplier<String> hello() {
        return () -> "hello";
    }

    @Bean
    public Supplier<Collection<Airport>> allairports() {
        return AirportsService::getAirports;
    }

    @Bean
    public Function<String, Collection<Airport>> airports() {
        return (filter) -> {
            if (StringUtils.isEmpty(filter)) {
                return AirportsService.getAirports();
            }
            else {
                return AirportsService.filter(filter);
            }

        };
    }

    @Bean
    public Function<String, Airport> airport() {
        return (code) -> AirportsService.getAirport(code.toUpperCase(Locale.US));
    }

}
