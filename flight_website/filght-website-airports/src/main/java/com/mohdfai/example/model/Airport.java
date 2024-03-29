package com.mohdfai.example.model;

import java.time.ZoneId;
import java.util.Comparator;

public class Airport {
    public static Comparator<Airport> cityNameComparator = Comparator.comparing(o -> o.city);
    private       String              code;
    private       String              name;
    private       String              city;
    private       String              country;
    private       Coordinates         coordinates;
    private       ZoneId              zoneId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZoneId() {
        return zoneId.getId();
    }

    public void setZoneId(String zoneId) {
        this.zoneId = ZoneId.of(zoneId);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "Airport{" + "code='" + code + '\'' + ", name='" + name + '\'' + ", city='" + city + '\'' + ", country='" + country + '\''
            + ", coordinates=" + coordinates + ", zoneId=" + zoneId + '}';
    }

}