package no.mesan.handterminator.model.maps;

import java.io.Serializable;

/**
 * @author Sondre Sparyby Boge
 * A single step out of many, between two locations defined by a Leg.
 */

public class Step implements Serializable {

	Polyline polyline;

	public Polyline getPolyline() {
		return polyline;
	}

	public void setPolyline(Polyline polyline) {
		this.polyline = polyline;
	}
}
