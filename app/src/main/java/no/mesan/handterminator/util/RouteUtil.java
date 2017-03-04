package no.mesan.handterminator.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.mesan.handterminator.RouteActivity;
import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.maps.DirectionResponse;
import no.mesan.handterminator.model.maps.Leg;
import no.mesan.handterminator.service.client.DirectionsClient;

/**
 * Contains several functions for manipulating the route and directions
 */
public class RouteUtil {

    private static final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

    // GoogleAddress displays the correct and complete addresses
    public static void setGoogleAddresses(Route route) {
        if(route == null)
            return;

        for(int i = 0; i < route.size(); i++) {
            Task t = route.getTask(i);
            Leg l = route.getLegs().get(i);

            t.setGoogleAddress(l.getEnd_address());
            t.setTime(l.getDuration().getValue());
            t.setDistance(l.getDistance().getValue());
        }
    }

    /**
     * Uses waypoint_order from directresult google maps
     * to rearrange the Tasks in the Route
     * @param route Route to take Tasks and waypoint_order from
     */
    public static void sortTasks(Route route) {

        List<Task> tasks = route.getTasks();
        if(tasks.size() == 0)
            return;

        List<Integer> order = route.getWaypointOrder();
        Log.d("DISTANCE_MATRIX", route.getWaypointOrder() + " sortTasks()");
        Log.d("DISTANCE_MATRIX", tasks.toString() + " tasks");
        List<Task> temp = new ArrayList<>();

        for(int i = 0; i < tasks.size(); i++)
            temp.add(tasks.get(i));

        tasks.clear();

        for(int i = 0; i < order.size(); i++)
            tasks.add(temp.get(order.get(i)));

        tasks.add(temp.get(temp.size() - 1));
        route.setTasks(tasks);

        Log.d("DISTANCE_MATRIX", tasks.toString() + " tasks");
    }

    /**
     * Uses waypoint_order from directresult google maps
     * to rearrange the TaskList
     * @param taskList
     * @param waypointOrder
     */
    public static void sortTasks(List<Task> taskList, List<Integer> waypointOrder) {
        if(taskList.size() == 0)
            return;

        List<Task> temp = new ArrayList<>();

        for(int i = 0; i < taskList.size(); i++)
            temp.add(taskList.get(i));

        taskList.clear();

        for(int i = 0; i < waypointOrder.size(); i++)
            taskList.add(temp.get(waypointOrder.get(i)));

        taskList.add(temp.get(temp.size() - 1));
    }

    /**
     * Combines a left and a right List<task> to a combined List<Task>
     * @param left
     * @param right
     * @return
     */
    public static void joinLeftRightTaskList(List<Task> tasks, List<Task> left, List<Task> right) {
        tasks.clear();
        for(Task t : left) tasks.add(t);
        for(Task t : right) tasks.add(t);
    }

    /**
     * Calculates the estimated delivery times of each task in the route
     * @param route current route
     * @param pos what task is the driver currently at? Only calculate from there on out.
     * @param delivery Then set deliverytime of current task to now if called at package delivery.
     */
    public static void estimateDeliveries(Route route, int pos, boolean delivery) {

        Date start = new Date();
        List<Leg> legs = route.getLegs();

        int i = 0;
        for (Leg leg : legs) {

            //Only update tasks that haven't been finished yet
            if (i >= pos) {
                //seconds to this task is to be delivered
                long min = leg.getDuration().getValue() * 1000;

                //saves the delivery time
                Date n = new Date(start.getTime() + min);

                //Update eta in task
                route.getTask(i).setEta(n);

                //adds transport time + 5 min for delivery
                start = new Date(n.getTime() + (5 * ONE_MINUTE_IN_MILLIS));
            }
            //If the method is called from the delivery button, the currently delivered object has
            //been delivered at this time, and it's eta is set to now.
            else if (delivery && i == pos-1)
                route.getTask(i).setEta(start);

            i++;
        }
    }
}
