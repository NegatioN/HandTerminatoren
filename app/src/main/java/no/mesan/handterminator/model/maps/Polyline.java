package no.mesan.handterminator.model.maps;

import java.io.Serializable;

/**
 * @author Sondre Sparby Boge
 * An string-encoded path between two points. Used for displaying route on map.
 */

public class Polyline implements Serializable{

	private String points;

	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}
}
