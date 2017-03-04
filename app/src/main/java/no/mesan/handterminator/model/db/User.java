package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

/**
 * @author Joakim Rishaug
 * Represents a profile owned by a single driver. Only one can be logged into device at the time.
 */
public class User extends SugarRecord<User>{

    private int profileIcon, hourSalary, packageSalary, drivingCompensation;
    private String firstname, lastname, login, password;
    private Statistics statistics;


    //TODO convert password to hash
    //TODO user is connected to a car/vehicle when logged in.
    //TODO user has statistics


    public User(){}
    /**
     *
     * @param profileIcon Profile-picture for the user.
     * @param firstname Firstname of user.
     * @param lastname Lastname of user.
     * @param login Username of the person
     * @param password Password for user.
     * @param hourSalary hourly salary of the user
     * @param packageSalary money user gets per package delivered
     * @param drivingCompensation money user gets per km driven in vehicle
     */
    public User(int profileIcon, String firstname, String lastname, String login, String password, int hourSalary, int packageSalary, int drivingCompensation){
        this.profileIcon = profileIcon;
        this.firstname = firstname;
        this.lastname = lastname;
        this.login = login;
        this.password = password;
        this.hourSalary = hourSalary;
        this.packageSalary = packageSalary;
        this.drivingCompensation = drivingCompensation;

        //generate new statistics-object for user
        this.statistics = new Statistics();
        this.statistics.save();
    }

    public int getIconId() {
        return profileIcon;
    }

    public int getProfileIcon() {
        return profileIcon;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Statistics getStatistics() {
        return statistics;
    }
    public int getHourSalary() {
        return hourSalary;
    }

    public int getPackageSalary() {
        return packageSalary;
    }

    public int getDrivingCompensation() {
        return drivingCompensation;
    }

    /**
     * Sets all salaries for the user
     * @param hourlySalary
     * @param packageSalary
     * @param drivingCompensation
     */
    public void setSalaries(int hourlySalary, int packageSalary, int drivingCompensation){
        this.hourSalary = hourlySalary;
        this.packageSalary = packageSalary;
        this.drivingCompensation = drivingCompensation;
        this.save();
    }
}
