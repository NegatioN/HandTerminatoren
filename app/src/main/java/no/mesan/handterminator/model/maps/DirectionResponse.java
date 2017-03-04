package no.mesan.handterminator.model.maps;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import no.mesan.handterminator.model.Route;

/**
 * @author Sondre Sparby Boge
 * Response from Google Directions API.
 */

public class DirectionResponse {

    private List<Route> routes;
    private String status;
    private String error_message;

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("routes", routes)
				.toString();
	}
}
