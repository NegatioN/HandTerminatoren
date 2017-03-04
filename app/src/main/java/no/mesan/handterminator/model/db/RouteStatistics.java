package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

/**
 * Created by NegatioN on 16.04.2015.
 */
public class RouteStatistics extends SugarRecord<RouteStatistics> {

    private long totalMeters, totalEarned, totalSeconds;
    private String name;
    private Statistics userstatistics;

    public RouteStatistics(){}

    public RouteStatistics(String name, long totalMeters, long totalEarned, long totalSeconds, Statistics userstatistics){
        this.name = name;
        this.totalMeters = totalMeters;
        this.totalEarned = totalEarned;
        this.totalSeconds = totalSeconds;
        this.userstatistics = userstatistics;

    }

    public long getTotalMeters() {
        return totalMeters;
    }

    public long getTotalEarned() {
        return totalEarned;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public String getName() {
        return name;
    }
}
