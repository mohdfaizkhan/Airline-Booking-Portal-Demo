package com.mohdfai.example.service;

import java.io.IOException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInitialization implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            FlightSchedulingService.loadFlightSchedule();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
