package no.mesan.handterminator.model.maps;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Singular point with latitude and longitude. Used as map-coordinate for addresses and steps.
 */

public class Point implements Serializable {

	private double lat;
	private double lng;

    public Point(){

    }

    public Point(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public Point(Point point){
        this.lat = point.lat;
        this.lng = point.lng;
    }

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public LatLng toLatLng() {
		return new LatLng(lat, lng);
	}

    public String toString(){
        return lat + ":" + lng;
    }
}
