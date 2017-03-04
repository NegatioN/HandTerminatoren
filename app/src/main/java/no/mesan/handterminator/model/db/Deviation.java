package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

/**
 * @author Joakim Rishaug
 * A deviation reported by a driver, for a certain Task.
 */
public class Deviation extends SugarRecord<Deviation>{
    //TODO define several different types of deviations
    private final static int LOST_PACKAGE = 0, WATER_DAMAGE = 1;
    //private final static String[] typeStrings = {"Mistet Pakke", "Fukt-skade"};

    private String info;
    private int type;
    private Task task;

    public Deviation(){}
    public Deviation(String info, int type, Task task){
        this.info = info;
        this.type = type;
        this.task = task;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
