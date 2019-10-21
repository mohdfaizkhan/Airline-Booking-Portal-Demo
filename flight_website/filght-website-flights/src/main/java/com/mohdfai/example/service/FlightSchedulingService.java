package com.mohdfai.example.service;

import au.com.bytecode.opencsv.CSVReader;
import com.mohdfai.example.model.Airport;
import com.mohdfai.example.model.FlightSchedule;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class FlightSchedulingService {
    private static Logger                            logger           = Logger.getLogger(FlightSchedulingService.class.getName());
    private static Map<String, List<FlightSchedule>> departingFlights = new HashMap<>();
    private static Map<String, List<FlightSchedule>> arrivingFlights  = new HashMap<>();

    static void loadFlightSchedule() throws IOException {
        if (departingFlights.isEmpty()) {
            InputStream inputStream = FlightSchedulingService.class.getResourceAsStream("/flights.csv");
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream), '\t');

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            reader.readAll().parallelStream()
                .map(nextLine -> {
                    FlightSchedule flightSchedule = new FlightSchedule();
                    flightSchedule.setFlightNumber(nextLine[1]);
                    flightSchedule.setDepartureAirport(nextLine[2]);
                    flightSchedule.setDepartureTime(LocalTime.parse(nextLine[3], formatter));
                    flightSchedule.setArrivalAirport(nextLine[4]);
                    flightSchedule.setArrivalTime(LocalTime.parse(nextLine[5], formatter));
                    return flightSchedule;
                })
                .forEach(flightSchedule -> {
                    departingFlights.computeIfAbsent(flightSchedule.getDepartureAirport(), s -> new ArrayList<>()).add(flightSchedule);
                    arrivingFlights.computeIfAbsent(flightSchedule.getArrivalAirport(), s -> new ArrayList<>()).add(flightSchedule);
                });
            logger.info("Populated " + departingFlights.size() + " departingFlights");
            logger.info("Populated " + arrivingFlights.size() + " arrivingFlights");

        }
    }

    static List<FlightSchedule[]> getRoutes(Map<String, Airport> airports, String origin, String destination) {
        List<FlightSchedule[]> routes = new ArrayList<>();
        List<FlightSchedule> departing = departingFlights.get(origin);
        List<FlightSchedule> arriving = arrivingFlights.get(destination);
        if (departing != null && arriving != null) {
            List<FlightSchedule> firstLegOptions = new ArrayList<>();
            for (FlightSchedule flightSchedule : departing) {
                if (flightSchedule.getArrivalAirport().equals(destination)) {
                    //Found a non-stop!
                    routes.add(new FlightSchedule[] { flightSchedule });
                }
                else {
                    firstLegOptions.add(flightSchedule);
                }
            }
            for (FlightSchedule firstLeg : firstLegOptions) {
                for (FlightSchedule secondLeg : arriving) {
                    if (connectingFlights(airports, firstLeg, secondLeg)) {
                        routes.add(new FlightSchedule[] { firstLeg, secondLeg });
                    }
                }
            }
        }

        for (FlightSchedule[] route : routes) {
            for (int i = 0; i < route.length; i++) {
                FlightSchedule schedule = route[i];
                schedule.setDepartureZoneId(airports.get(schedule.getDepartureAirport()).getZoneId());
                schedule.setArrivalZoneId(airports.get(schedule.getArrivalAirport()).getZoneId());
            }
        }

        return routes;
    }

    private static boolean connectingFlights(Map<String, Airport> airports, FlightSchedule firstLeg, FlightSchedule secondLeg) {
        if (firstLeg.getArrivalAirport().equals(secondLeg.getDepartureAirport())) {
            Duration layover = getPositiveDuration(firstLeg.getArrivalTime(), secondLeg.getDepartureTime());
            Duration minLayover = Duration.ofMinutes(30);
            Duration maxLayover = getPositiveDuration(firstLeg.getDepartureTime(), airports.get(firstLeg.getDepartureAirport()).getZoneId(),
                firstLeg.getArrivalTime(), airports.get(firstLeg.getArrivalAirport()).getZoneId());
            return layover.compareTo(minLayover) >= 0 && layover.compareTo(maxLayover) <= 0;
        }
        else {
            return false;
        }
    }

    private static Duration getPositiveDuration(LocalTime startTime, LocalTime endTime) {
        return getPositiveDuration(startTime, ZoneId.systemDefault(), endTime, ZoneId.systemDefault());
    }

    private static Duration getPositiveDuration(LocalTime startTime, ZoneId startZone, LocalTime endTime, ZoneId endZone) {
        ZonedDateTime start = ZonedDateTime.of(LocalDate.now(), startTime, startZone);
        ZonedDateTime end = ZonedDateTime.of(LocalDate.now(), endTime, endZone);
        if (end.isBefore(start)) {
            end = end.plus(1, ChronoUnit.DAYS);
        }
        return Duration.between(start, end);
    }
}