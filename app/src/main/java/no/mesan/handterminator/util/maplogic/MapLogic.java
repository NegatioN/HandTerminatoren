package no.mesan.handterminator.util.maplogic;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.maps.Bounds;
import no.mesan.handterminator.util.MapUtil;

/**
 * @author Sondre Sparby Boge
 *
 * Super-class for MapLogic, contains functions and listeners
 * for Google Maps. Specific tasks is handled by the sub-classes.
 */
public class MapLogic implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener  {

    // MapFragment received from activity or fragment
    SupportMapFragment fragmentMap;

    // Markers on each delivery/pick-up spot on the map
    Marker lastOpened = null;
    List<Marker> markers = new ArrayList<>();

    boolean firstCameraChange = true;
    public Route route; // containing all info needed for drawing route on map
    protected GoogleMap map;

    // Zoom-scale for the map, smaller number means less zoom
    protected int scale = 32;
    // Vertical offset, positive value shifts the map downwards
    protected double displacement = 5.0;

    public MapLogic(SupportMapFragment map, Route route){
        fragmentMap = map;
        fragmentMap.getMapAsync(this);
        this.route = route;
    }

    // Gets GoogleMap-object from Google
    public void setMap() {
        if (fragmentMap != null) {
            fragmentMap.getMapAsync(this);
        }
    }

    /**
     * Sets up marker-listeners, called after map is received from Google
     * @param map GoogleMap-object to apply changes to
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        // listener for marker-click on the map, used for detecting which element is clicked
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (lastOpened != null) {
                    lastOpened.hideInfoWindow();
                    if (lastOpened.equals(marker)) {
                        lastOpened = null;
                        return true;
                    }
                }
                marker.showInfoWindow();
                lastOpened = marker;

                // gets index of selected item, will be used later
                //int index = getTaskListIndex(marker.getTitle());

                return true;
            }
        });

        // avoid markers not showing after map-click then marker-click
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lastOpened = null;
            }
        });

        map.setOnCameraChangeListener(this);
    }

    /**
     * Moves camera to specified location with correct zoom for the first time the map is ready
     * @param cameraPosition required default param, not used
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (firstCameraChange) {
            if (map != null && route != null)
                MapUtil.moveCamera(map, MapUtil.translateBounds(route.getBounds(), scale, displacement));

            firstCameraChange = false;
        }
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    // Overridden by sub-classes
    public void updateRoute(Route route) { }


    // used everytime a change to the map has been issued
    public void updateMap(Route route) {
        if (route == null) return;

        map.clear();
        updateRoute(route);

        Bounds bounds = MapUtil.translateBounds(route.getBounds(), scale, displacement);
        MapUtil.moveCamera(map, bounds);
    }


    // resets the camera-position with the route as centre
    public void centerMap() {
        if(map == null || route == null) return;
        Bounds bounds = MapUtil.translateBounds(route.getBounds(), scale, displacement);
        MapUtil.moveCamera(map, bounds);
    }
}