package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.model.maps.Bounds;
import no.mesan.handterminator.model.maps.Leg;
import no.mesan.handterminator.model.maps.Point;

/**
 * @author Joakim Rishaug
 * This is only the database-representation of the Route. Route-objects live in the program through Route
 * Owns one or several Tasks, and keeps the map-points of these tasks respectively.
 */
public class DBRoute extends SugarRecord<DBRoute> implements Serializable {

    public enum Shift{ DAY, NIGHT};

    private boolean gDirectionsCalled;

    private String routeName;
    private double northlat, northlng, southlat, southlng; //used for bounds

    private int distance;
    private int estTime;
    private String shift;
    private String status;
    private boolean completed;
    private int routeLength;                //number of tasks in the route
    private boolean isAcceptableScanned;

    public DBRoute() {
    }

    public DBRoute(String routeName, Shift shift) {
        this.routeName = routeName;
        gDirectionsCalled = false;
        distance = 0;
        estTime = 0;
        routeLength = 0;
        completed = false;
        status = "Ikke påbegynt";
        this.shift = shift.name();
        this.isAcceptableScanned = false;

    }

    /**
     * @return all tasks owned by this route in db.
     */
    public List<Task> getTasks() {
        return Task.find(Task.class, "dbroute = ?", String.valueOf(this.getId()));
    }

    /**
     * @return all tasks of the given route that are deliveries from db.
     */
    public List<Task> getDeliveryTasks() {
        return Task.find(Task.class, "dbroute = ? and type = ?", String.valueOf(this.getId()), String.valueOf(Task.TASK_DELIVERY));
    }

    public void addTask(Task task) {
        task.setParentRoute(this);
        routeLength++;
    }

    /**
     * Creates a Bounds-ojbect for the dbRoute
     *
     * @return Bounds made out of four doubles.
     */
    public Bounds getBounds() {
        Point northPoint = new Point(northlat, northlng);
        Point southPoint = new Point(southlat, southlng);
        Bounds bounds = new Bounds(northPoint, southPoint);
        return bounds;
    }

    /**
     * Creates four doubles and saves them in the dbroute.
     *
     * @param bounds google maps Bounds-object
     */
    public void setBounds(Bounds bounds) {
        this.northlat = bounds.getNortheast().getLat();
        this.northlng = bounds.getNortheast().getLng();
        this.southlat = bounds.getSouthwest().getLat();
        this.southlng = bounds.getSouthwest().getLng();
        this.save();
    }

    /**
     * Effectively a toString() of the route name.
     * @return name of route
     */
    public String getName() {
        return routeName;
    }

    public int getDistance() {
        return distance;
    }

    /**
     * Sets the total distance of the route based on distance of each leg in it.
     * @param legs A list of legs
     */
    public void setDistance(List<Leg> legs) {
        int estimatedMeters = 0;
        for (Leg leg : legs) {
            //estimated distance in meters
            estimatedMeters += leg.getDistance().getValue();
        }
        this.distance = estimatedMeters;
        this.save();
    }

    public int getEstTime() {
        return estTime;
    }


    public String getRouteName() {
        return routeName;
    }

    /**
     * Sets an estimated total time for the route based on indicidal legs estimated time.
     * Also adds 5 minutes of estimated time per leg.
     * @param legs a list of legs from google diretions.
     */
    public void setEstTime(List<Leg> legs) {
        int estimatedTime = 0;
        for (Leg leg : legs) {
            //estimated duration + 5 minutes "delay per stop"
            estimatedTime += (leg.getDuration().getValue() + (5 * 60));
        }
        this.estTime = estimatedTime;
        this.save();
    }
    /**
     * Has the route already called google directions and has marker-points + bounds?
     */
    public boolean isgDirectionsCalled() {
        return gDirectionsCalled;
    }

    /**
     * sets google directions called to true/false
     * @param gDirectionsCalled boolean for google directions call
     */
    public void setgDirectionsCalled(boolean gDirectionsCalled) {
        this.gDirectionsCalled = gDirectionsCalled;
        this.save();
    }

    /**
     * Current implementation only supports day/night shifts
     * @return is shift == Day
     */
    public boolean isDayShift(){
        return shift.equals(Shift.DAY.name());
    }

    public void setShift(Shift shift){
        this.shift = shift.name();
        this.save();
    }

    /**
     * Method used to not flood Google Matrix-API with calls. Ensures one call per unchanged route
     * @return the matrix containing the time-cost of each option in the route.
     */
    public int[][] getMatrix(){
        ArrayList<MatrixValue> matrixValues = new ArrayList<>(getMatrixValues());
        //uses length+1 because we include the starting-position in the matrix
        int[][] costMatrix = new int[routeLength+1][routeLength+1];
        int arrayListIndex = 0;
        for(int i = 0; i < routeLength+1; i++){
            for(int j = 0; j < routeLength+1; j++){
                costMatrix[i][j] = matrixValues.get(arrayListIndex++).getValue();
            }
        }
        return costMatrix;
    }

    /**
     * Updates the matrix associated with DbRoute in db.
     * Needs to be called every time we add a new Task, and want to use directions/maps api
     * @param costMatrix
     */
    public void addMatrix(int[][] costMatrix){
        for(MatrixValue matrixValueInDb : getMatrixValues())
            matrixValueInDb.delete();
        this.save();
        int estimatedArrayLength = (routeLength+1)*(routeLength+1);
            ArrayList<MatrixValue> matrixValues = new ArrayList<>();

            //add entire matrix to array
            for(int i = 0; i < routeLength+1; i++){
                for(int j = 0; j < routeLength+1; j++){
                    matrixValues.add( new MatrixValue(costMatrix[i][j], this));
                }
            }

            this.save();

    }

    /**
     * @return matrix-values from db in a list
     */
    private List<MatrixValue> getMatrixValues(){
        return MatrixValue.find(MatrixValue.class, "parentroute = ?", String.valueOf(this.getId()));
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean isCompleted) {
        this.completed = isCompleted;
        this.save();
    }

    public boolean isAcceptableScanned() {
        return isAcceptableScanned;
    }

    public void setAcceptableScanned(boolean isAcceptableScanned) {
        this.isAcceptableScanned = isAcceptableScanned;
        this.save();
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
        this.save();
    }
}
