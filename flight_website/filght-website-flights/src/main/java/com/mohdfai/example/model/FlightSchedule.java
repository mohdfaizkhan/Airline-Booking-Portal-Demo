package com.mohdfai.example.model;

import java.time.LocalTime;
import java.time.ZoneId;

public class FlightSchedule {
    private String    flightNumber;
    private String    departureAirport;
    private LocalTime departureTime;
    private ZoneId    departureZoneId;

    private String    arrivalAirport;
    private LocalTime arrivalTime;
    private ZoneId    arrivalZoneId;

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public ZoneId getDepartureZoneId() {
        return departureZoneId;
    }

    public void setDepartureZoneId(ZoneId departureZoneId) {
        this.departureZoneId = departureZoneId;
    }

    public ZoneId getArrivalZoneId() {
        return arrivalZoneId;
    }

    public void setArrivalZoneId(ZoneId arrivalZoneId) {
        this.arrivalZoneId = arrivalZoneId;
    }

    @Override
    public String toString() {
        return "FlightSchedule{" + "flightNumber='" + flightNumber + '\'' + ", departureAirport='" + departureAirport + '\''
            + ", departureTime=" + departureTime + ", arrivalAirport='" + arrivalAirport + '\'' + ", arrivalTime=" + arrivalTime + '}';
    }
}