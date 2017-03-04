package no.mesan.handterminator.service.client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import no.mesan.handterminator.RouteActivity;
import no.mesan.handterminator.model.maps.DirectionResponse;
import no.mesan.handterminator.service.DirectionsService;
import no.mesan.handterminator.view.ProgressWheel;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Sondre Sparby Boge
 * Client that requests directions from Google Directions API.
 */

@EBean
public class DirectionsClient {

    RouteActivity context;

    @Background
    public void getDirections(RouteActivity context, String origin, String destination, String waypoints) {
        this.context = context;

        Callback<DirectionResponse> callback = new Callback<DirectionResponse>() {
            @Override
            public void success(DirectionResponse response, Response status) {
                if(!response.getStatus().equals("OK")) {
                    // something went wrong
                    Log.d("DirectionResponse", "status: " + response.getStatus());
                    Log.d("DirectionResponse", "error_message: " + response.getError_message());
                    handleStatusException(response.getStatus());
                }

                updateUI(response);
            }


            @Override
            public void failure(RetrofitError error) {
                showAlertDialog(error);
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com/maps/api/directions")
                .build();

        DirectionsService directionsService = restAdapter.create(DirectionsService.class);
        directionsService.getDirections(origin, destination, waypoints, "no", callback);
    }

    @UiThread
    void updateUI(DirectionResponse result) {
        context.updateRoute(result.getRoutes().get(0));
    }

    @UiThread
    void showAlertDialog(RetrofitError error) {
        Log.d("ERROR!", error.getMessage());

        new AlertDialog.Builder(context)
                .setTitle("Noe gikk galt!")
                .setMessage(error.getMessage())
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Handles response-status != "OK"
    private void handleStatusException(String status) {
        if(status.equals("NOT_FOUND")) {
            // at least one location could not be geolocated
        } else if(status.equals("ZERO_RESULTS")) {
            // no route could be found between origin and destination
        } else {
            // other exception
        }
    }
}
