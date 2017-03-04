package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

import java.util.List;

/**
 * @author Joakim Rishaug
 * The class represents a drivers statistics in the database. easily implementable for multiple users
 * standard CRUD
 */
public class Statistics extends SugarRecord<Statistics> {

    private long metersDriven;  //total meters driven by user
    private long timeSpent;     //Total time spent driving a route
    private long packagesDelivered; //num packages delivered
    private double moneyEarned; // money earned. moneyEarned() = timelønn*timer+pakkelønn*pakker+km kjørt*kjørekompensasjon
    private double totalDrivingReimbursed, totalHourlySalaray, totalPackageSalary;
    private long emergenciesCalledIn; //number of emergencies called in
    private long emergenciesHelped; //number of emergencies user has assisted in.
    private long deviationsReported; //number of deviations reported by the user on packages

    public Statistics(){
        metersDriven = 0;
        timeSpent = 0;
        packagesDelivered = 0;
        moneyEarned = 0;
        emergenciesCalledIn = 0;
        emergenciesHelped = 0;
        deviationsReported = 0;
    }

    public Statistics(long metersDriven, long timeSpent, long packagesDelivered, double moneyEarned,
                      long emergenciesCalledIn, long emergenciesHelped, long deviationsReported){
        this.metersDriven = metersDriven;
        this.timeSpent = timeSpent;
        this.packagesDelivered = packagesDelivered;
        this.moneyEarned = moneyEarned;
        this.emergenciesCalledIn = emergenciesCalledIn;
        this.emergenciesHelped = emergenciesHelped;
        this.deviationsReported = deviationsReported;
    }

    public long getMetersDriven() {
        return metersDriven;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public long getPackagesDelivered() {
        return packagesDelivered;
    }


    public double getMoneyEarned() {
        return moneyEarned;
    }

    public void addMetersDriven(long meters){
        this.metersDriven += meters;
        this.save();
    }
    public void addTimeSpent(long time){
        this.timeSpent += time;
        this.save();
    }
    public void addPackagesDelivered(long packages){
        this.packagesDelivered += packages;
        this.save();

    }
/*
    public void addMoneyEarned(long money){
        this.moneyEarned += money;
        this.save();
    }
    */

    /**
     * Increments emergencies called in
     */
    public void addEmergencyCalled(){
        this.emergenciesCalledIn++;
        this.save();
    }

    /**
     * Increments emergencies helped
     */
    public void addEmergencyHelped(){
        this.emergenciesHelped++;
        this.save();
    }

    /**
     * Increments deviations
     */
    public void addDeviationReported(){
        this.deviationsReported++;
        this.save();
    }

    /**
     * Calculates hourly wage based on wage * hours.
     * @param wage user's hourly wage
     * @param hours hours worked by user
     * @return sum earned total
     */
    public static double calculateHourlyMoney(double wage, double hours){
        return hours*wage;
    }

    /**
     * Calculates money earned by delivering x packages
     * @param packageSalary money user recieves for delivering a package
     * @param packages number of packages delivered
     * @return sum earned total
     */
    public static long calculatePackageMoney(long packageSalary, long packages){
        return packageSalary*packages;
    }

    /**
     * Calculate how much a user is reimbursed for current drive
     * @param meters meters driven
     * @return money reimbursed
     */
    public static double reimburseDriving(int meters){
        //reimburse 13kr per km driven
        return (meters/1000D)*13D;
    }

    /**
     * Gets all routetatistics associated with this user's statistics
     * @return list of routestatistics
     */
    public List<RouteStatistics> getRouteStatistics(){
        return RouteStatistics.find(RouteStatistics.class, "userstatistics = ?", String.valueOf(this.getId()));
    }

    public void addDrivingReimbursed(double reimbursed){
        totalDrivingReimbursed += reimbursed;
        moneyEarned+= reimbursed;
        this.save();
    }

    public void addPackageSalarayEarned(double packageSalary){
        totalPackageSalary += packageSalary;
        moneyEarned += packageSalary;
        this.save();
    }
    public void addHourSalaryEarned(double hourSalary){
        totalHourlySalaray += hourSalary;
        moneyEarned += hourSalary;
        this.save();
    }

    public double getTotalDrivingReimbursed() {
        return totalDrivingReimbursed;
    }

    public double getTotalHourlySalaray() {
        return totalHourlySalaray;
    }

    public double getTotalPackageSalary() {
        return totalPackageSalary;
    }

    public long getEmergenciesCalledIn() {
        return emergenciesCalledIn;
    }

    public long getEmergenciesHelped() {
        return emergenciesHelped;
    }

    public long getDeviationsReported() {
        return deviationsReported;
    }
}
