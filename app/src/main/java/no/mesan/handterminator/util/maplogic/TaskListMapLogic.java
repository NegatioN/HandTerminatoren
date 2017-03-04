package no.mesan.handterminator.util.maplogic;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.maps.Bounds;
import no.mesan.handterminator.util.MapUtil;
import no.mesan.handterminator.util.PrintUtil;

/**
 * @author Sondre Sparby Boge
 *
 * Extends MapLogic, and contains specific functions for the TaskList.
 * It's initialized in TaskListFragment, with the mapFragment & textViews
 * as parameters, since this class is not a Fragment.
 */
public class TaskListMapLogic extends MapLogic {

    // Fields for the map-header
    TextView map_head_address, map_head_distance, map_head_time, map_head_progress;

    List<Polyline> lines;

    /**
     * Initiate logic-class.
     * @param map, mapFragment
     * @param route, route-object
     * Other params is header-TextViews
     */
    public TaskListMapLogic(SupportMapFragment map, Route route, TextView adr, TextView dist, TextView time, TextView prog) {
        super(map, route);
        fragmentMap = map;
        map_head_address = adr;
        map_head_distance = dist;
        map_head_time = time;
        map_head_progress = prog;
        fragmentMap.getMapAsync(this);
        this.route = route;
        scale = 8;
        displacement = 2.1;
    }

    /**
     * Sets up map-listeners and positions after the map is received from Google
     * @param map GoogleMap-object to apply changes to
     */
    @Override
    public void onMapReady(GoogleMap map) {
        super.onMapReady(map);
        // sets the default map-location to Norway instead of Africa
        LatLng location = new LatLng(59.913, 10.752);

        // custom listener for the MyLocationButton, in top-right on the map
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                centerMap();
                return false;
            }
        });

        map.setOnCameraChangeListener(this);
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLng(location));

        // runs when map is finished rendering
//        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                updateMap(0);
//                updateRoute(0);
//                updateMapHeader(0);
//            }
//        });
    }

    /**
     * Shows info-window for the marker belonging to selected Task in TaskList
     * @param index, index of selected task
     */
    public void onTaskListClick(int index) {
        if(index >= markers.size()) return;
        Marker marker = markers.get(index+1);

        // hide current opened marker
        if (lastOpened != null) {
            lastOpened.hideInfoWindow();
            // reset current opened marker if it's clicked again
            if (lastOpened.equals(marker)) {
                lastOpened = null;
                return;
            }
        }
        // open newly clicked marker
        marker.showInfoWindow();
        lastOpened = marker;
    }

    /**
     * Clears and redraws route and markers, also updates the map-header.
     * Issued after delivery or after a directions-call.
     * @param currentTask, index of the current active task
     */
    public void updateRoute(int currentTask) {
        if(map == null || currentTask < 0) return;

        map.clear();
        markers.clear();

        new MapUtil().drawRoute(map, route, currentTask, markers);
        updateMapHeader(currentTask);
    }

    /**
     * Updates the map-card (map-header) to display info of the current task
     * @param currentTask, index of the current active task
     */
    private void updateMapHeader(int currentTask) {
        // if completed route
        if(currentTask >= route.size()){
            map_head_address.setText("Levering fullført");
            map_head_distance.setText(0 + " km");
            map_head_time.setText(0 + " min");
            map_head_progress.setText(route.size() + "/" + route.size());
            return;
        }

        map_head_address.setText(route.getTask(currentTask).getAddress());
        map_head_distance.setText(PrintUtil.displayDistance((int)route.getTask(currentTask).getDistance()));
        map_head_time.setText(PrintUtil.displayTime((int)route.getTask(currentTask).getTime()));
        map_head_progress.setText(currentTask + 1 + "/" + route.size());
    }

    /**
     * Clears the map and updates it to display the current task.
     * Issued after a Google Directions-call.
     * @param currentTask, index of the current active task
     */
    public void updateMap(int currentTask) {
        if (route == null) return;

        map.clear();
        updateRoute(currentTask);

        Bounds bounds = MapUtil.translateBounds(route.getBounds(), scale, displacement);
        MapUtil.moveCamera(map, bounds);
    }
}