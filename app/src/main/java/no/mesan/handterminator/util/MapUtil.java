package no.mesan.handterminator.util;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.maps.Bounds;
import no.mesan.handterminator.model.maps.Leg;
import no.mesan.handterminator.model.maps.Point;
import no.mesan.handterminator.model.maps.Step;

/**
 * Contains several functions interacting and drawing on the map.
 */

public class MapUtil {

    // draws the whole route with given parameters
    public static void drawRoute(GoogleMap map, Route route, int nextTask, List<Marker> markers) {
        /**
         * NextTask will always be index 1 or higher.
         */

        //MARKERS
        // marker - start
        Marker marker = addMarker(map, route.getLegs().get(0).getStart_address(), route.getLegs().get(0).getStartLocation(), BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        if (marker != null)
            markers.add(marker);

        Marker nextTaskMarker = null;
        for (int i = 0; i < route.size(); i++) {
            if (i < nextTask)       // Markers that are already done
                marker = addMarker(map, route.getLegs().get(i).getEnd_address(), route.getLegs().get(i).getEndLocation(), BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            else if (i > nextTask)   // Markers for future tasks
                marker = addMarker(map, route.getLegs().get(i).getEnd_address(), route.getLegs().get(i).getEndLocation(), null);

            if (marker != null)
                markers.add(marker);
        }

        if (nextTask < route.size())   // Marker for next task. renders last to make it always be on top to avoid confusion
            markers.add(addMarker(map, route.getLegs().get(nextTask).getEnd_address(), route.getLegs().get(nextTask).getEndLocation(), BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        //LINES (path)
        for (int i = 0; i < route.size(); i++) {
            PolylineOptions plo = new PolylineOptions();
            List<Step> steps;

            // lines that are already done
            if (i < nextTask) {
                steps = route.getLegs().get(i).getSteps();
                plo.color(Color.GRAY);

                for (int j = 0; j < steps.size(); j++)
                    plo.addAll(MapUtil.decodePoly(steps.get(j).getPolyline().getPoints()));
            }
            // lines for future tasks
            else if (i > nextTask) {
                steps = route.getLegs().get(i).getSteps();
                plo.color(Color.rgb(0, 127, 191));

                for (int j = 0; j < steps.size(); j++)
                    plo.addAll(MapUtil.decodePoly(steps.get(j).getPolyline().getPoints()));
            }
            // line - current
            else if (nextTask < route.size()) {
                steps = route.getLegs().get(nextTask).getSteps();
                plo.color(Color.rgb(0, 223, 63));

                for (int j = 0; j < steps.size(); j++)
                    plo.addAll(MapUtil.decodePoly(steps.get(j).getPolyline().getPoints()));
            }

            //Draw polyline for this leg
            drawPolyLine(map, plo);
        }
    }

    // Puts markers on the map
    public static void drawMarkers(GoogleMap map, Route route, List<Marker> markers) {

        // Start marker
        Marker marker = addMarker(map, route.getLegs().get(0).getStart_address(), route.getLegs().get(0).getStartLocation(), BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        if (marker != null) markers.add(marker);

        // All other markers
        for (int i = 0; i < route.getLegs().size(); i++) {
            marker = addMarker(map, route.getLegs().get(i).getEnd_address(), route.getLegs().get(i).getEndLocation(), null);
            if (marker != null) markers.add(marker);
        }
    }

    /**
     * draws points on the map for each Task in the given Route
     *
     * @param map   the google map
     * @param route a from the database.
     */
    public static void drawMarkersFromDb(GoogleMap map, DBRoute route) {
        List<Task> taskList = route.getTasks();
        for (Task task : taskList) {
            //uses default color-argument for marker
            addMarker(map, task.getName(), task.getPoint(), null);
        }
    }

    /**
     * Saves the point for a marker for a given task on the map.
     *
     * @param position position of the task in the taskList
     * @param task     the task-object to save point to.
     * @param legs     the legs of the route returned from google directions
     */
    public static void saveTaskPoint(int position, Task task, List<Leg> legs) {
        Point point;
        if (position == 0)
            point = legs.get(position).getStartLocation();
        else
            point = legs.get(position).getEndLocation();
        task.setPoint(point);
    }


    // Draw a single polyline with the give PolylineOptions on the map
    public static Polyline drawPolyLine(GoogleMap map, PolylineOptions plo) {
        plo.width(8).geodesic(true);
        Polyline polyline = map.addPolyline(plo);
        return polyline;
    }

    // sets a single marker with given parameters
    public static Marker addMarker(GoogleMap map, String title, Point point, BitmapDescriptor color) {
        if (point == null || map == null) return null;
        if (color == null)
            return map.addMarker(new MarkerOptions().position(point.toLatLng()).title(title));
        else
            return map.addMarker(new MarkerOptions().position(point.toLatLng()).icon(color).title(title));
    }

    // decodes string to List<LatLan>, used to decode paths from Google Directions
    private static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    // moves camera on given map with bounds as outer limits
    public static void moveCamera(GoogleMap map, Bounds bounds) {
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(getLatLngBounds(bounds), padding);
        map.moveCamera(cu);
    }

    private static LatLngBounds getLatLngBounds(Bounds bounds) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(bounds.getNortheast().toLatLng());
        builder.include(bounds.getSouthwest().toLatLng());

        return builder.build();
    }

    // used to zoom out on the map a little, with scale and displacement (vertical offset) as changable parameter
    public static Bounds translateBounds(Bounds oldbounds, int scale, double displacement) {
        double delta_lng = Math.abs(oldbounds.getNortheast().getLng() - oldbounds.getSouthwest().getLng());
        double delta_lat = Math.abs(oldbounds.getNortheast().getLat() - oldbounds.getSouthwest().getLat());

        Bounds bounds = new Bounds();
        Point ne = new Point();
        Point sw = new Point();
        ne.setLat(oldbounds.getNortheast().getLat() + delta_lat / scale * displacement);
        ne.setLng(oldbounds.getNortheast().getLng() + delta_lng / scale);
        sw.setLat(oldbounds.getSouthwest().getLat() - delta_lat / scale / displacement);
        sw.setLng(oldbounds.getSouthwest().getLng() - delta_lng / scale);

        bounds.setNortheast(ne);
        bounds.setSouthwest(sw);

        return bounds;
    }

    public static Point getPointFromAddress(String address, Context context) {
        Geocoder coder = new Geocoder(context);
        try {
            Address result = coder.getFromLocationName(address, 1).get(0);
            return new Point(result.getLatitude(), result.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Called form taskListFragment. Used to show a temporary marker for a task on the map.
     *
     * @param task    Task to create marker for
     * @param map     Currently used map
     * @param context Context of call
     * @return Returns the created marker for the given task.
     */
    public static Marker showTempMarker(Task task, GoogleMap map, Context context) {
        String address = task.getAddress() + ", " + task.getZip() + ", " + task.getCity();
        Point p = getPointFromAddress(address, context);
        BitmapDescriptor color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);

        Marker marker = addMarker(map, task.getAddress(), p, color);
        marker.showInfoWindow();
        return marker;
    }

    /**
     * Expand the camerview to show the given marker if it is currently out of bounds
     * Used with showTempMarker in taskListFragment
     *
     * @param map    Currently used map
     * @param bounds Current bounds
     * @param marker Marker to be shown in expanded view
     */
    public static void expandCameraView(GoogleMap map, Bounds bounds, Marker marker) {
        int padding = 200; // offset from edges of the map in pixels

        double mLat = marker.getPosition().latitude, mLong = marker.getPosition().longitude;
        Point ne = new Point(bounds.getNortheast()), sw = new Point(bounds.getSouthwest());

        if (mLat > bounds.getNortheast().getLat())
            ne.setLat(mLat);
        if (mLat < bounds.getSouthwest().getLat())
            sw.setLat(mLat);
        if (mLong > bounds.getNortheast().getLng())
            ne.setLng(mLong);
        if (mLong < bounds.getSouthwest().getLng())
            sw.setLng(mLong);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(getLatLngBounds(new Bounds(ne, sw)), padding);
        map.moveCamera(cu);
    }
}