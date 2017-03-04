package no.mesan.handterminator;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.util.List;

import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.service.client.DirectionsClient;
import no.mesan.handterminator.util.RouteUtil;
import no.mesan.handterminator.view.ProgressWheel;

/**
 * @author Lars-Erik Kasin, Sondre Sparby Boge
 *
 * Super-class to most activities that requires either the
 * Route or Google maps. It can request calls to Google Directions
 * for directions of drawn paths on Google Maps.
 */
public class RouteActivity extends ActionBarActivity {

    /**
     * The class should only hold a route-object if it is in "NavDrawerAcvitity". Otherwise it
     * should use dbRoutes. It will try getting the TaskList of a Route/dbRoute
     */
    Route route;
    DBRoute dbRoute;

    ProgressWheel progressWheel;

    // If current Direction-call is doing a partial optimization
    boolean partialOptimized = false;
    // If route have already been sorted
    boolean isAlreadySorted = false;

    // working as a mutex-lock for route-calculation
    boolean isCalculating = false;

    public Route getRoute() {
        return route;
    }

    /**
     * Handles the Route-result from Google Directions,
     * specifically saving the result and preparing it
     * for the list and map. Also if the route is half-finished
     * it will do a partial-optimization instead of a full-optimization
     * using an exstra Google Directions-call
     * @param route result from Google Directions
     */
    public void updateRoute(Route route) {
        List<Task> tasks;
        List<Integer> waypointOrder = route.getWaypointOrder();

        // Received call for drawing the whole route
        if (this.route != null) {
            tasks = this.route.getTasks();

            // No previous Directions call using tasks
            if(tasks == null)
                tasks = dbRoute.getTasks();
        }
        else { // No previous calls on Directions, only draw markers
            tasks = dbRoute.getTasks();
        }

        // Set route, waypoints and tasks
        route.setTasks(tasks);

        if(waypointOrder != null) route.setWaypointOrder(waypointOrder);
        this.route = route;

        // Partial-sort
        if(partialOptimized) {
            // Split taskList into two parts: finished and unfinished
            List<Task> unfinished = route.getUnfinishedTasks();
            List<Task> finished = route.getFinishedTasks();

            // Sort unfinished tasks and then join both lists back together
            RouteUtil.sortTasks(unfinished, waypointOrder);

            RouteUtil.joinLeftRightTaskList(tasks, finished, unfinished);

            isAlreadySorted = true;
        } else if(!isAlreadySorted) { // Standard-sort
            RouteUtil.sortTasks(tasks, waypointOrder);

            isAlreadySorted = true;
        }

        isCalculating = false;

        // Request another directions call if last call was a partial-optimization
        if(partialOptimized)
            requestDirectionsUpdate(route, false);
        else {
            RouteUtil.setGoogleAddresses(route);
            progressWheel.stopSpinning();
        }
    }

    /**
     * Calls for Google Directions, handles if there's a need for a second Google Directions-call
     * @param route route containing List<Task> to be optimized and/or drawn
     * @param optimize whether or not the route will be optimized by time
     */
    public void requestDirectionsUpdate(Route route, boolean optimize) {
        if (!isCalculating) {
            isCalculating = true;
            progressWheel.spin();
            partialOptimized = optimize;
            isAlreadySorted = !optimize;

            List<Task> tasks;

            // Use partial optimization if any tasks is already finished
            if(partialOptimized && route.getFinishedTasks().size() > 0)
                tasks = route.getUnfinishedTasks();
            // else use standard optimization
            else {
                tasks = route.getTasks();
                partialOptimized = false;
            }

            // Generate String-waypoints and make the Google Directions call
            String waypoints = route.generateWaypointsString(tasks, optimize, Mockup.START_ADDRESS);
            DirectionsClient client = new DirectionsClient();
            client.getDirections(this, Mockup.START_ADDRESS, route.getDestinationAddress(), waypoints);
        }
    }

    /**
     * Creates a temporary Route-object from the DBRoute-id and makes a Google Directions-call
     * @param dbRouteId id for the route to be optimized and/or drawn
     * @param optimize whether or not the route will be optimized by time
     */
    public void requestDirectionsUpdate(long dbRouteId, boolean optimize) {
        if (!isCalculating) {
            progressWheel.spin();
            this.dbRoute = DBRoute.findById(DBRoute.class, dbRouteId);
            Route tempRoute = new Route(dbRoute);

            requestDirectionsUpdate(tempRoute, optimize);
        }
    }

    /**
     * Transfers tasks onto the new google-returned Route, sorts these after optimized route and
     * includes the adresses of each task onto it.
     *
     * @param route Route to treat
     */
    public void treatGoogleRoute(Route route) {
        if (route != null) {
            //Update route's tasks and sort them
            //RouteUtil.sortTasks(route);
            RouteUtil.setGoogleAddresses(route);
        }
    }

}
