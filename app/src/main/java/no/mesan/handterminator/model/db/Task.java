package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import no.mesan.handterminator.model.maps.Point;

/**
 * @author Lars-Erik Kasin, Joakim Rishaug, Sondre Sparby Boge
 *         <p/>
 *         Defines a Task (a single delivery or retrieval), that contains X packages
 *         in addition to owner and sender and Google Maps required info.
 */
public class Task extends SugarRecord<Task> implements Serializable {
    //attributes for deliveries
    public final static int TASK_DELIVERY = 0, TASK_PICKUP = 1;

    private String city, zip;
    private String address, name;
    private String googleAddress;
    private int type;
    private int size;
    private int phone;
    private double lat, lng;    //latitude and longitude for creating a point

    private long pickupTimeStart = 0, pickupTimeEnd = 0;
    private long time, distance;
    private boolean sentSMS = false;

    private boolean finished = false;
    private boolean active = false;
    private Date eta;
    private Person sender, receiver;
    private DBRoute dbroute;

    public Task() {
    }

    //the full Task
    public Task(int type, String city, String zip, String address, String name, String googleAddress,
                long pickupTimeStart, long pickupTimeEnd, int phone, Date eta, long time, long distance, Person sender, Person receiver, DBRoute dbroute) {
        this.type = type;
        this.city = city;
        this.zip = zip;
        this.address = address;
        this.pickupTimeStart = pickupTimeStart;
        this.pickupTimeEnd = pickupTimeEnd;
        this.name = name;
        this.eta = eta;
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.distance = distance;
        this.dbroute = dbroute;
    }

    //the full Task - without pickuptime
    public Task(int type, String city, String zip, String address, String name,
                Person sender, Person receiver, DBRoute dbroute) {
        this.type = type;
        this.city = city;
        this.zip = zip;
        this.address = address;
        this.name = name;
        this.sender = sender;
        this.receiver = receiver;
        this.dbroute = dbroute;
    }

    public Date getEta() {
        return eta;
    }

    public void setEta(Date eta) {
        this.eta = eta;
        this.save();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        this.save();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        this.save();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        this.save();
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
        this.save();
    }

    public void setGoogleAddress(String gaddress) {
        googleAddress = gaddress;
        this.save();
    }

    public String getGoogleAddress() {
        return googleAddress;
    }


    public Person getSender() {
        return sender;
    }

    public void setSender(Person sender) {
        this.sender = sender;
        this.save();
    }

    public Person getReceiver() {
        return receiver;
    }

    public void setReceiver(Person receiver) {
        this.receiver = receiver;
        this.save();
    }

    public List<Package> getPackages() {
        List<Package> packages = Package.find(Package.class, "task = ?", String.valueOf(this.getId()));
        this.size = packages.size();
        return packages;
    }

    public Package getPackage(long id) {
        return Package.findById(Package.class, id);

    }

    public int getSize() {
        return size;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
        this.save();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
        this.save();
    }

    public boolean getSentSMS() {
        return sentSMS;
    }

    public void setSentSMS(boolean b) {
        sentSMS = b;
        this.save();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.save();
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setParentRoute(DBRoute dbroute) {
        this.dbroute = dbroute;
        this.save();
    }

    public void setPoint(Point point) {
        this.lat = point.getLat();
        this.lng = point.getLng();
        this.save();
    }

    public Point getPoint() {
        return new Point(lat, lng);
    }

    /**
     * @return a list of all deviations reported on the given task in db.
     */
    public List<Deviation> getDeviations() {
        return Deviation.find(Deviation.class, "task = ?", String.valueOf(this.getId()));
    }

    public void setPickupTimeStart(long pickupTimeStart) {
        this.pickupTimeStart = pickupTimeStart;
        this.save();

    }

    public void setPickupTimeEnd(long pickupTimeEnd) {
        this.pickupTimeEnd = pickupTimeEnd;
        this.save();

    }

    /**
     * @return the start-time for the package
     */
    public long getTimeSlotStart() {
        return pickupTimeStart;
    }

    /**
     * @return the end-time for the package
     */
    public long getTimeSlotEnd() {
        return pickupTimeEnd;
    }

    /**
     * @return the start-time for the package in String
     */
    public String getTimeSlotStartString() {
        return pickupTimeStart == 0 ? "" : new SimpleDateFormat("HH:mm").format(new Date(pickupTimeStart));
    }

    /**
     * @return the end-time for the package in String
     */
    public String getTimeSlotEndString() {
        return pickupTimeEnd == 0 ? "" : new SimpleDateFormat("HH:mm").format(new Date(pickupTimeEnd));
    }

    public String toString() {
        return address;
    }

    public DBRoute getDbRoute() {
        return dbroute;
    }

    public boolean isWithinTimeslot(){
        if (pickupTimeStart > 0)
            return pickupTimeStart < eta.getTime() && eta.getTime() < pickupTimeEnd;
        else
            return true;
    }
}

