package no.mesan.handterminator.model;

/**
 *  @author Martin Hagen
 *  This class is only for POC reasons and will not be saved in db. This is also why this class dont
 *  extend Person
 *
 *  Used for list of drivers in emergency handlig - drivers(nearby) you can call to get help
 */
public class Driver {
    String name, number, distance;

    public Driver(String name, String number, String distance) {
        this.name = name;
        this.number = number;
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
