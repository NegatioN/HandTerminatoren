package no.mesan.handterminator.util.maplogic;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.maps.Bounds;
import no.mesan.handterminator.util.MapUtil;

/**
 * @author Sondre Sparby Boge
 *
 * Extends MapLogic, and contains specific methods for the RouteList
 * It's initialized in RouteListActivity, with the mapFragment
 * as parameters, since this class is not an Activity itself.
 */
public class RouteListMapLogic extends MapLogic {

    public RouteListMapLogic(SupportMapFragment map, Route route){
        super(map, route);
        scale = 32;
        displacement = 5.0;
    }

    /**
     * Sets default camera position on the map, and disables moving/zooming
     * @param map GoogleMap-object to apply changes to
     */
    @Override
    public void onMapReady(GoogleMap map) {
        super.onMapReady(map);

        LatLng location = new LatLng(59.913, 10.752);
        map.setOnCameraChangeListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLng(location));

        // avoid moving or zooming camera
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
    }

    /**
     * Clears and redraws markers on the map
     * @param route route-object to get task-position from
     */
    @Override
    public void updateRoute(Route route) {
        ArrayList<Marker> marks = new ArrayList<>();
        new MapUtil().drawMarkers(map, route, marks);
    }

    /**
     * Clears the map and updates it to display the current route.
     * Issued after a Google Directions-call.
     * @param route route-object to get tasks and bounds from
     */
    public void updateMap(Route route) {
        if (route == null) return;

        map.clear();
        updateRoute(route);

        Bounds bounds = MapUtil.translateBounds(route.getBounds(), scale, displacement);
        MapUtil.moveCamera(map, bounds);
    }

    /**
     * Updates the map-markers and placement.
     * @param dbRoute dbRoute-object to get bounds and tasks from.
     */
    public void updateMap(DBRoute dbRoute){
        map.clear();
        MapUtil.drawMarkersFromDb(map, dbRoute);

        Bounds bounds = MapUtil.translateBounds(dbRoute.getBounds(), scale, displacement);
        MapUtil.moveCamera(map, bounds);
    }
}