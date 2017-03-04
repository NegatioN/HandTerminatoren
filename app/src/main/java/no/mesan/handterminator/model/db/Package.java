package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * @author Joakim Rishaug
 * Defines a single package within a delivery/Task.
 */
public class Package extends SugarRecord<Package> implements Serializable{
    private String kolli;
    private int height, width;
    private double weight;
    private Task task;
    private boolean scannedIn, scannedOut;

    public Package() {
    }

    public Package(String kolli, int height, int width, double weight, Task task) {
        this.kolli = kolli;
        this.height = height;
        this.width = width;
        this.weight = weight;
        this.task = task;
    }

    public String getKolli() {
        return kolli;
    }

    public void setKolli(String kolli) {
        this.kolli = kolli;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

//TODO implement transfer package to different delivery (maybe use delete and remake on other delivery)

    /**
     * Transfers this package to a different delivery
     * @param task Task to transfer to.
     */
    public void transferOwnership(Task task){
        this.task = task;
        this.save();
    }

    /**
     * Transfers this package to a different delivery
     * @param taskId ID of the delivery to transfer to.
     */
    public void transferOwnership(long taskId){
        Task task = Task.findById(Task.class, taskId);
        transferOwnership(task);
    }

    /**
     * Is the package scanned in and ready for delivery?
     * @return true/false
     */
    public boolean isScannedIn() {
        return scannedIn;
    }

    public void setScannedIn(boolean scanned) {
        this.scannedIn = scanned;
        this.save();
    }

    /**
     * is the package scanned out and delivered to the customer?
     * @return true/false
     */
    public boolean isScannedOut() {
        return scannedOut;
    }

    public void setScannedOut(boolean scanned) {
        this.scannedOut = scanned;
        this.save();
    }



}
