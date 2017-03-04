package no.mesan.handterminator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Package;
import no.mesan.handterminator.model.db.Person;
import no.mesan.handterminator.model.db.RouteStatistics;
import no.mesan.handterminator.model.db.Statistics;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;

/**
 * Used for making mockup-info
 */
public class Mockup {

    public static final String START_ADDRESS = "Vallegata 10, Oslo";
    private static final String[] streets = {"Agdergata", "Sarpsborggata", "Skogroveien", "Snargangen", "Sportsveien", "Sunnmørgata", "Rådhusplassen", "Trondheimsveien",
            "Storgata", "Henrik Ibsens gate", "Schults gate", "Moldegata", "Oslogata", "Dronningens gate", "Karl Johans gate", "Bekkeliveien", "Montebelloveien", "Stubberudveien",
            "Elmholtveien", "Elgveien", "Ensjøsvingen", "Filerveien", "Fossilveien", "Fritzners gate"};
    private static final String[] firstNames = {"Tore", "Harald", "Gjertrud", "Martin", "Sondre", "Joakim", "Lars-Erik", "Trine", "Guppy"};
    private static final String[] lastNames = {"Tang", "Kulø", "Hardy", "Hageen", "Boge", "Rishaug", "Kasin", "Potter", "Kempf", "Vestlie", "Tangen", "Østlie", "Spondresen"};
    private static final String[] taskNames = {"Oddleif Berg", "Marine Harvest A/S", "TrafikkJentene A/S", "Ingolf Thoresen", "AntikkTing AS", "Ingebjørg Garborg", "Spoom Company",
            "Misjonsforbundet", "Humanistforbundet", "Glassmagasinet", "Elsa Birken", "Energima", "FUNKSJONELL VVS", "Holtan Last", "Implenia", "ISS Facility", "Komplett AS", "Lysebu AS",
            "Malermestern AS", "Nikita Hair", "Oslo Kemnerkontor", "Palmgren AS"};
    private static final String[] routeNames = {"Oslo", "Oslo Sentrum"};

    private static final int MAX_TASKS = 7;
    private static List<Integer> streetNames;

    public Route generateTestRoute(){
        Route route = new Route();
        List<Integer> waypoint_order = new ArrayList<>();

        for(int i = 0; i < MAX_TASKS; i++){
            waypoint_order.add(i);
        }

        route.setTasks(Task.listAll(Task.class));
        route.setWaypointOrder(waypoint_order);

        return route;
    }

    public void generateDBRoutes(){

        generateUser();

        for(int i = 0; i < 7; i++){
            generateRoute(routeNames[0] + " - " + (i+1) , i);
        }
    }

    private void generateUser() {
        User user = new User(R.drawable.ic_profile,"Tore", "Tang", "toret", "ang", 200, 25, 15);
        user.save();
        Statistics toreStats = user.getStatistics();
        String[] fakeRouteNames = {"Oslo Sentrum", "Oslo Vest", "Oslo Øst", "Lier", "Hobøl"};
        for(int i = 0; i < fakeRouteNames.length; i++){
            generateFakeRouteStats(fakeRouteNames[i], toreStats);
        }

    }
    //quickly generates fake statistics to showcase user profile easier
    private void generateFakeRouteStats(String routeName, Statistics userStats){
        Random randomGen = new Random();
        long metersDriven = randomGen.nextInt(30000) + 20000;
        long secondsSpent = randomGen.nextInt(4000) +3600;
        double moneyEarned = randomGen.nextInt(1100)+700;

        userStats.addDrivingReimbursed(moneyEarned*0.12D);
        userStats.addHourSalaryEarned(moneyEarned*0.55D);
        userStats.addPackageSalarayEarned(moneyEarned*0.33D);
        userStats.addTimeSpent(secondsSpent);
        userStats.addMetersDriven(metersDriven);

        new RouteStatistics(routeName, metersDriven,(long) moneyEarned,secondsSpent, userStats).save();
    }

    public void generateRoute(String routeName, int index ) {
        Random ran = new Random();
        DBRoute.Shift shift = ran.nextInt(30) % 2 == 0 ? DBRoute.Shift.DAY : DBRoute.Shift.NIGHT;
        DBRoute route = new DBRoute(routeName, shift);
        route.save();

        List<Integer> numInArray = createRandomizedOrder(taskNames.length);
        streetNames = createRandomizedOrder(streets.length);


        for(int i = 0; i < MAX_TASKS; i++){
            createTask(route, numInArray.get(i % MAX_TASKS));
        }
    }

    public Task createTask( DBRoute route, int index){
        Random random = new Random();
        String taskName;
        if(index < 0){
            taskName = createTaskName(random.nextInt(taskNames.length));
        }
        else
            taskName = createTaskName(index);
        Person recipient = createPerson();

        long pickupStart = 0;
        long pickupEnd = 0;
        if(random.nextInt(4) == 0) {
            pickupStart = (new Date(2015,4,15,14,30)).getTime();
            pickupEnd = (new Date(2015,4,15,16,0)).getTime();

        }
        int streetIndex;
        if(index == -1) //case create ad-hoc pakke
            streetIndex = random.nextInt(streets.length);
        else //case vanlig hent/lever pakke
            streetIndex = streetNames.get(index);
        int type = random.nextInt(4); // 1 in x - chance for pick-up
        if(type > 1) type = 0;
        Task task = new Task(type,"oslo", "0045", createAddress(streetIndex), taskName, "test", pickupStart, pickupEnd, Integer.parseInt(recipient.getPhone()), new Date(),25L, 25L, recipient, createPerson(), route);
        task.save();    //must be above createpackages since it gets referenced with task.getId()
        createPackages(task);

        return task;
    }

    private String createTaskName(int random) {
        return taskNames[random];
    }

    public void createPackages(Task task){
        int numPackages = (int)(Math.random()*2)+1;
        int counter = 0;
        while(numPackages > counter){
            makeRandomPackage(task);
            counter++;
        }

    }

    //Helper method for generating a random package.
    public Package makeRandomPackage(Task task){
        Random ran = new Random();
        int kollinr = ran.nextInt(1000000000) + 100000;
        int height = ran.nextInt(100);
        int width = ran.nextInt(100);
        double weight = ran.nextInt(10);

        String kol = kollinr+"";
        int l = kol.length();
        for(int i = l; i < 10; i++)
            kol = 0 + kol;

        Package pack = new Package("" + kol, height, width, weight,task);
        pack.save();
        return pack;
    }

    //creates a random street address from an array of streets
    public String createAddress(int streetIndex){
        String address = streets[streetIndex] + " 10";
        return address;
    }

    public Person createPerson(){
        Random ran = new Random();
        String name = firstNames[ran.nextInt(firstNames.length)] + " " + lastNames[ran.nextInt(lastNames.length)];
        int phone = ran.nextInt(89999999) + 10000000;
        Person person = new Person(name, createAddress(ran.nextInt(streets.length)), "oslo", phone + "", "" + 0044);
        person.save();
        return person;
    }

    // Generates test-route to be used with route-optimalization testing
    public DBRoute generateRouteOptimalizationTestRoute() {
        DBRoute route = new DBRoute("F1 Oslo", DBRoute.Shift.DAY);
        route.save();

        long[] pickStart = new long[]   {0, 0, 0, 0, 0, 0, 0, 0};
        long[] pickEnd = new long[]     {0, 0, 0, 0, 0, 0, 0, 0};

        Random random = new Random();

        for(int i = 0; i < 5; i++) {
            Person recipient = createPerson();

            String taskName = createTaskName(random.nextInt(taskNames.length));

            long pickupStart = 0;
            long pickupEnd = 0;
            if(pickStart[i] != 0) {
                pickupStart = (new Date(2015, 4,15, (int) pickStart[i], 0)).getTime();
                pickupEnd = (new Date(2015, 4, 15, (int) pickEnd[i], 0)).getTime();
            }
            int type = random.nextInt(4); // 1 in x - chance for pick-up
            if(type > 1) type = 0;
            Task task = new Task(type,"oslo", "0045", streets[i] + " " + 10, taskName, "test", pickupStart, pickupEnd, Integer.parseInt(recipient.getPhone()), new Date(),25L, 25L, recipient, createPerson(), route);
            task.save();    //must be above createpackages since it gets referenced with task.getId()
            createPackages(task);
        }

        return route;
    }

    //asumes 0 to be start-index in all cases
    private static List<Integer> createRandomizedOrder(int maxIndex){
        List<Integer> numInArray = new ArrayList<>();
        for(int i = 0; i < maxIndex; i++){
            numInArray.add(i);
        }
        Collections.shuffle(numInArray); //randomize arrayPosition
        return numInArray;
    }
}
