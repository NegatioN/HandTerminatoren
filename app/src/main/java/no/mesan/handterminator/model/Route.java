package no.mesan.handterminator.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.maps.Bounds;
import no.mesan.handterminator.model.maps.Leg;
import no.mesan.handterminator.model.maps.Polyline;

/**
 * @author Sondre Sparby Boge, Joakim Rishaug
 * Route of several legs. Also contains bounds and overall polyline for the entire route.
 * This route-object represents the route when it's "alive" in the program.
 * Gets passed back times from Google Directions whenever an API-call is made.
 */

public class Route implements Serializable {
    private Bounds bounds;
    private List<Leg> legs;
    private List<Task> tasks;

    public List<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }

    private List<Long> taskIds; //used for transferring non-serializable task-ids through acitivites.

    public Route(){}
    public Route(DBRoute route){
        this.tasks = route.getTasks();
        this.bounds = route.getBounds();
    }


    @SerializedName("overview_polyline")
    private Polyline overviewPolyline;

    private List<Integer> waypoint_order;

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

    public Polyline getOverviewPolyline() {
        return overviewPolyline;
    }

    public void setOverviewPolyline(Polyline overviewPolyline) {
        this.overviewPolyline = overviewPolyline;
    }

    public List<Integer> getWaypointOrder() {
        return waypoint_order;
    }

    public void setWaypointOrder(List<Integer> waypoint_order) {
        this.waypoint_order = waypoint_order;
    }

    public Task getTask(int position){
        return tasks.get(position);
    }

    public void addTask(Task task) {
        tasks.add(tasks.size()-1, task);
        tasks.get(0).getDbRoute().addTask(task);
    }

    public String getDestinationAddress(){
        Task t = tasks.get(tasks.size()-1);
        return t.getAddress() + "+" + t.getCity();
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public int size(){
        return legs.size();
    }

    public String shortString(){
        return "Route " + tasks.size();
    }

    @Override
    public String toString() {
        String x = "Route har " + tasks.size() + " tasks, og " + legs.size() + " legs.";

        x += "\nTasks: ";
        for (Task t: tasks){
            x += t.getName() + ", ";
        }

        x += "\nLegs: ";
        for (Leg t: legs){
            x += t.getEnd_address() + ", ";
        }
        return x;
    }

    /**
     * Extract an array of addresses from a list of tasks and then convert to a waypoint string.
     * @param tasks list of tasks to be converted
     * @param optimize should the list be optimized?
     * @param startAddress the start address of the route
     * @return returns the waypoint string
     */
    public String generateWaypointsString(List<Task> tasks, boolean optimize, String startAddress) {
        String[] addresses = new String[tasks.size()+1];

        //Add startpoint!
        addresses[0] = startAddress;

        for(int i = 0; i < tasks.size(); i++) {
            addresses[i + 1] = tasks.get(i).getAddress() + ", " + tasks.get(i).getCity();
            // Log.d("Address of task", adresses[i + 1]);
        }

        //Convert String array to waypoint string
        String waypoints = "optimize:" + String.valueOf(optimize) + "|"; // true for optimized, false for default list-order

        for (int i = 1; i < addresses.length - 1; i++) {
            waypoints += addresses[i];
            waypoints += "|";
        }

        return waypoints;
    }

    /**
     * Extracts addresses from a List<Task> and converts them to a waypoint string
     * @param taskList to be converted to waypoints
     * @return waypoin string
     */
    public String generateMatrixWaypoints(List<Task> taskList) {
        return generateMatrixWaypoints(taskList, "");
    }

    /**
     * Extracts addresses from a List<Task> and converts them to a waypoint-string
     * @param taskList to be converted to waypoints
     * @param startAddress the starting point if any, should equal "" if none
     * @return waypoint string
     */
    public String generateMatrixWaypoints(List<Task> taskList, String startAddress) {
        String waypoints = "";

        // adds addresses from taskList to waypoint-string
        for(int i = 0; i < tasks.size(); i++) waypoints += tasks.get(i).getAddress() + ", " + tasks.get(i).getCity() + "|";

        // adds start-address to the start if any
        if(!startAddress.trim().equals("")) waypoints = startAddress + "|" + waypoints;

        return waypoints;
    }

    public List<Task> getFinishedTasks() {
        List<Task> finished = new ArrayList<>();
        for(Task t : tasks) if(t.isFinished()) finished.add(t);
        return finished;
    }

    public List<Task> getUnfinishedTasks() {
        List<Task> unfinished = new ArrayList<>();
        for(Task t : tasks) if(!t.isFinished()) unfinished.add(t);
        return unfinished;
    }
}
