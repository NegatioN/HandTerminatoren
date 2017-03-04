package no.mesan.handterminator.model.maps;

import java.io.Serializable;

/**
 * @author Sondre Sparby Boge
 * Used by Google Maps. Defines the borders of the map.
 * Contains two Points
 */

public class Bounds implements Serializable {

	private Point northeast;
	private Point southwest;
    public Bounds(){}

    public Bounds(Point north, Point south){
        this.northeast = north;
        this.southwest = south;
    }

	public Point getNortheast() {
		return northeast;
	}

	public void setNortheast(Point northeast) {
		this.northeast = northeast;
	}

	public Point getSouthwest() {
		return southwest;
	}

	public void setSouthwest(Point southwest) {
		this.southwest = southwest;
	}

    public String toString(){
        return "Norteast: " + northeast.toString() + " - Southwest: " + southwest.toString();
    }
}
