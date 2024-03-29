package com.mohdfai.example.service;

import com.mohdfai.example.model.Airport;
import com.mohdfai.example.model.Flight;
import com.mohdfai.example.model.FlightQuery;
import com.mohdfai.example.model.FlightSegment;
import com.mohdfai.example.model.Itinerary;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import rx.Observable;

@org.springframework.web.bind.annotation.RestController
@RibbonClients({ @RibbonClient(name = "airports"), @RibbonClient(name = "flights"), @RibbonClient(name = "sales") })
public class API_GatewayController {

    private static Logger logger = Logger.getLogger(API_GatewayController.class.getName());

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @Value("${hystrix.threadpool.SalesThreads.coreSize}")
    private int threadSize;

    private static void populateFormattedTimes(Flight[] flights, Map<String, Airport> airports) {
        for (Flight flight : flights) {
            for (FlightSegment segment : flight.getSegments()) {
                segment
                    .setFormattedDepartureTime(getFormattedTime(segment.getDepartureTime(), airports.get(segment.getDepartureAirport())));
                segment.setFormattedArrivalTime(getFormattedTime(segment.getArrivalTime(), airports.get(segment.getArrivalAirport())));
            }
        }
    }

    private static String getFormattedTime(Instant departureTime, Airport airport) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma");
        formatter = formatter.withLocale(Locale.US);
        formatter = formatter.withZone(ZoneId.of(airport.getZoneId()));
        return formatter.format(departureTime);
    }

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RequestMapping(value = "/airportCodes", method = RequestMethod.GET)
    public String[] airports() {
        Airport[] airports = restTemplate.getForObject("http://gateway/airports/allairports", Airport[].class);
        String[] airportDescriptors = new String[airports.length];
        for (int index = 0; index < airportDescriptors.length; index++) {
            Airport airport = airports[index];
            airportDescriptors[index] = airport.getCode() + "\t" + airport.getCity() + " - " + airport.getName();
        }
        return airportDescriptors;
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public List<Itinerary> query(@RequestParam("departureDate") String departureDate,
        @RequestParam(required = false, value = "returnDate") String returnDate, @RequestParam("origin") String origin,
        @RequestParam("destination") String destination, HttpServletRequest request) {
        long queryTime = System.currentTimeMillis();
        FlightQuery flightQuery = new FlightQuery();
        flightQuery.date = departureDate;
        flightQuery.origin = origin;
        flightQuery.destination = destination;

        Flight[] departingFlights = restTemplate.postForObject("http://gateway/flights/query", flightQuery, Flight[].class);
        logger.info("Found " + departingFlights.length + " departing flights");
        Map<String, Airport> airports = getAirportMap();
        populateFormattedTimes(departingFlights, airports);
        List<Itinerary> departingItineraries = getPricing(departingFlights);
        List<Itinerary> pricedItineraries;
        if (returnDate == null) {
            pricedItineraries = departingItineraries;
        }
        else {
            FlightQuery flightQuery2 = new FlightQuery();
            flightQuery2.date = returnDate;
            flightQuery2.origin = origin;
            flightQuery2.destination = destination;

            Flight[] returnFlights = restTemplate.postForObject("http://gateway/flights/query", flightQuery2, Flight[].class);
            logger.info("Found " + returnFlights.length + " returning flights");
            populateFormattedTimes(returnFlights, airports);
            List<Itinerary> returningItineraries = getPricing(departingFlights);
            pricedItineraries = new ArrayList<>();
            for (Itinerary departingItinerary : departingItineraries) {
                for (Itinerary returningItinerary : returningItineraries) {
                    Itinerary itinerary = new Itinerary(departingItinerary.getFlights()[0], returningItinerary.getFlights()[0]);
                    itinerary.setPrice(departingItinerary.getPrice() + returningItinerary.getPrice());
                    pricedItineraries.add(itinerary);
                }
            }
        }
        pricedItineraries.sort(Itinerary.durationComparator);
        pricedItineraries.sort(Itinerary.priceComparator);
        logger.info("Returning " + pricedItineraries.size() + " flights");
        logger.info("Query method took " + (System.currentTimeMillis() - queryTime) + " milliseconds in total!");
        return pricedItineraries;
    }

    private @NotNull List<Itinerary> getPricing(Flight[] itineraries) {
        long pricingTime = System.currentTimeMillis();
        List<Itinerary> pricedItineraries = new ArrayList<>();
        for (int index = 0; index < itineraries.length; ) {
            List<Observable<Itinerary>> observables = new ArrayList<>();
            int batchLimit = Math.min(index + threadSize, itineraries.length);
            for (int batchIndex = index; batchIndex < batchLimit; batchIndex++) {
                observables.add(new PricingCall(itineraries[batchIndex]).toObservable());
            }
            logger.info("Will price a batch of " + observables.size() + " tickets");
            Observable<Itinerary[]> zipped = Observable.zip(observables, objects ->
            {
                Itinerary[] priced = new Itinerary[objects.length];
                for (int batchIndex = 0; batchIndex < objects.length; batchIndex++) {
                    priced[batchIndex] = (Itinerary) objects[batchIndex];
                }
                return priced;
            });
            Collections.addAll(pricedItineraries, zipped.toBlocking().first());
            index += threadSize;
        }
        logger.info("It took " + (System.currentTimeMillis() - pricingTime) + " milliseconds to price " + itineraries.length + " tickets");
        return pricedItineraries;
    }

    private Map<String, Airport> getAirportMap() {
        Airport[] airports = restTemplate.getForObject("http://gateway/airports/allairports", Airport[].class);
        return Arrays.stream(airports).collect(Collectors.toMap(Airport::getCode, airport -> airport));
    }

    private class PricingCall extends HystrixCommand<Itinerary> {
        private Flight flight;

        PricingCall(Flight flight) {
            super(HystrixCommandGroupKey.Factory.asKey("Sales"), HystrixThreadPoolKey.Factory.asKey("SalesThreads"));
            this.flight = flight;
        }

        @Override
        protected Itinerary run() throws Exception {
            try {
                return restTemplate.postForObject("http://gateway/sales/price", flight, Itinerary.class);
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Failed!", e);
                throw e;
            }
        }

        @Override
        protected Itinerary getFallback() {
            logger.warning("Failed to obtain price, " + getFailedExecutionException().getMessage() + " for " + flight);
            return new Itinerary(flight);
        }
    }
}
