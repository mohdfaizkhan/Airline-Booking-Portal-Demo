package com.mohdfai.example.service;

import au.com.bytecode.opencsv.CSVReader;
import com.mohdfai.example.model.Airport;
import com.mohdfai.example.model.Coordinates;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AirportsService {
    private static Logger               logger   = Logger.getLogger(AirportsService.class.getName());
    private static Map<String, Airport> airports = new HashMap<>();

    public static void loadAirports() throws IOException {
        if (airports.isEmpty()) {
            InputStream inputStream = AirportsService.class.getResourceAsStream("/airports.csv");
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream), '\t');

            airports = reader.readAll().parallelStream()
                .map(nextLine -> {
                    Airport airport = new Airport();
                    airport.setName(nextLine[0]);
                    airport.setCity(nextLine[1]);
                    airport.setCountry(nextLine[2]);
                    airport.setCode(nextLine[3]);
                    airport.setCoordinates(new Coordinates(nextLine[5], nextLine[6], nextLine[7]));
                    airport.setZoneId(nextLine[10]);
                    return airport;
                })
                .collect(Collectors.toMap(Airport::getCode, e -> e))
            ;
            logger.info("Populated " + airports.size() + " airports");
        }
    }

    public static Airport getAirport(String code) {
        Airport airport = airports.get(code);
        logger.info("Got " + airport + " for " + code);
        return airport;
    }

    public static Collection<Airport> getAirports() {
        return Collections.unmodifiableCollection(airports.values());
    }

    public static Set<String> getAirportCodes() {
        return Collections.unmodifiableSet(airports.keySet());
    }

    public static Collection<Airport> filter(final String filter) {
        return airports.values().stream()
            .filter(airport -> airport.getCity().toUpperCase(Locale.US).startsWith(filter.toUpperCase(Locale.US)))
            .collect(Collectors.toList())
            ;
    }
}