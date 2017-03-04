package no.mesan.handterminator.util;

import java.text.DecimalFormat;
import java.util.List;

import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Package;
import no.mesan.handterminator.model.db.Task;

/**
 * The class is used for methods relating to treating objects for printing/display in app.
 * Created by NegatioN on 12.03.2015.
 */
public class PrintUtil {
    private static String[] timeNames = {"sekunder", "minutter", "timer"};
    /**
     * Converts seconds to a string with either x secs,mins, hours.
     * @param seconds seconds to convert
     * @return String with x secs, mins, hours
     */
    public static String displayTime(long seconds){
        long result = seconds;
        int timesDevisable = 0;
        while(result >= 60 && timesDevisable < 2){
            result /= 60;
            timesDevisable +=1; // hours
        }
        if(timesDevisable == 2) {
            long minutes = (seconds % 3600)/60; //remainder after hours divided by 60 secs
            return (int)result + " " + timeNames[timesDevisable] + " " +  minutes + " " + timeNames[timesDevisable-1];
        }
        else if(timesDevisable == 1){
            return (int)result + " " + timeNames[1];
        }

        return result + " " + timeNames[timesDevisable];
    }

    /**
     * Converts meters to km in string-format
     * @param meters integer for meters
     * @return string with "x km"
     */
    public static String displayDistance(long meters){
        return (meters/1000) + " km";
    }

    /**
     * Display input as input + kr
     * @param money money to display
     * @return money-string "x kr"
     */
    public static String displayMoney(long money){
        return money + " kr";
    }
    public static String displayMoney(double money){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(money) + " kr";
    }

    public static String printRouteListKolli(List<DBRoute> dbRoute) {
        String out = "";
        for(DBRoute route : dbRoute)
            out += "\n" + route.getRouteName() + ":\n" + printTaskListKollis(route.getTasks());
        return out;
    }

    public static String printTaskListKollis(List<Task> taskList) {
        String out = "";

        for(Task t : taskList) {
            for(Package p : t.getPackages()) {
                out += p.getKolli() + "\n";
            }
        }

        return out;
    }
}
