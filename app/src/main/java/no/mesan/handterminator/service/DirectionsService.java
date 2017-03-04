package no.mesan.handterminator.service;

import no.mesan.handterminator.model.maps.DirectionResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * @author Sondre Sparyby Boge
 * Service requests directions from Google Directions API.
 */

public interface DirectionsService {
    @GET("/json")
    void getDirections(@Query("origin") String origin, @Query("destination") String destination,
									@Query("waypoints") String waypoints, @Query("language") String language, Callback<DirectionResponse> callback);
}
