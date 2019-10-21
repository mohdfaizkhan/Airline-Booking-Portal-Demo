package com.mohdfai.example.service;

import com.mohdfai.example.model.Airport;
import com.mohdfai.example.model.Flight;
import com.mohdfai.example.model.FlightQuery;
import com.mohdfai.example.model.FlightSegment;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * @author S750976
 */
@RefreshScope
@Configuration
@RibbonClient(name = "airports")
public class ExposedConfig {

    private static Logger            logger        = Logger.getLogger(ExposedConfig.class.getName());
    private        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * value example
     */
    @Bean
    public Supplier<String> hello() {
        return () -> "hello";
    }

    private Flux<Airport> clientCall() {
        return webClientBuilder
            .baseUrl("http://gateway/airports/allairports")
            .build()
            .get()
            .retrieve()
            .bodyToFlux(Airport.class)
            .doOnError(error -> logger.info(error.getMessage()))
            ;
    }

    @Bean
    public Function<Flux<FlightQuery>, Flux<Flight>> query() {

        return (flightQuery) -> flightQuery
            .flatMap(queryValue ->
                clientCall()
                    .reduce(new HashMap<String, Airport>(), (previous, current) -> {
                        previous.putIfAbsent(current.getCode(), current);
                        return previous;
                    })
                    .flatMapMany(stringAirportMap -> Flux.fromIterable(FlightSchedulingService
                        .getRoutes(stringAirportMap, queryValue.origin, queryValue.destination)))
                    .map(flightSchedules -> {
                        LocalDate travelDate = LocalDate.parse(queryValue.date, dateFormatter);
                        FlightSegment[] segments = new FlightSegment[flightSchedules.length];
                        for (int index = 0; index < segments.length; index++) {
                            segments[index] = new FlightSegment();
                            segments[index].setFlightNumber(Integer.parseInt(flightSchedules[index].getFlightNumber()));
                            segments[index].setDepartureAirport(flightSchedules[index].getDepartureAirport());
                            segments[index].setArrivalAirport(flightSchedules[index].getArrivalAirport());
                            //For now assume all travel time is for the requested date and not +1.
                            segments[index].setDepartureTime(
                                getInstant(travelDate, flightSchedules[index].getDepartureTime(),
                                    flightSchedules[index].getDepartureZoneId()));
                            segments[index].setArrivalTime(
                                getInstant(travelDate, flightSchedules[index].getArrivalTime(), flightSchedules[index].getArrivalZoneId()));
                        }
                        //Fix the timestamp when date is the next morning
                        Instant previousTimestamp = segments[0].getDepartureTime();
                        for (FlightSegment segment : segments) {
                            if (previousTimestamp.isAfter(segment.getDepartureTime())) {
                                segment.setDepartureTime(segment.getDepartureTime().plus(1, ChronoUnit.DAYS));
                            }
                            previousTimestamp = segment.getDepartureTime();
                            if (previousTimestamp.isAfter(segment.getArrivalTime())) {
                                segment.setArrivalTime(segment.getArrivalTime().plus(1, ChronoUnit.DAYS));
                            }
                            previousTimestamp = segment.getArrivalTime();
                        }
                        Flight flight = new Flight();
                        flight.setSegments(segments);
                        return flight;
                    })
                    .doOnError(error -> logger.info(error.getMessage()))
                    .doOnNext(flight -> {
                        if (flight.getSegments().length == 1) {
                            logger.info("Nonstop:\t" + flight.getSegments()[0]);
                        }
                        else {
                            logger.info("One stop\n\t" + flight.getSegments()[0] + "\n\t" + flight.getSegments()[1]);
                        }
                    }))
            .doOnError(error -> logger.info(error.getMessage()))
            ;

    }

    private Instant getInstant(LocalDate travelDate, LocalTime localTime, ZoneId zoneId) {
        return ZonedDateTime.of(travelDate, localTime, zoneId).toInstant();
    }

}
