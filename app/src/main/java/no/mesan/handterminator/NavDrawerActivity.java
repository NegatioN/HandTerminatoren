package no.mesan.handterminator;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.mesan.handterminator.fragment.TaskListFragment;
import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.RouteStatistics;
import no.mesan.handterminator.model.db.Statistics;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;
import no.mesan.handterminator.service.client.DirectionsClient;
import no.mesan.handterminator.util.AnimationUtil;
import no.mesan.handterminator.util.CommunicationUtil;
import no.mesan.handterminator.util.DialogUtil;
import no.mesan.handterminator.util.InstructionsUtil;
import no.mesan.handterminator.util.NavDrawerUtil;
import no.mesan.handterminator.view.ProgressWheel;

/**
 * Created by NegatioN on 28.01.2015.
 */
//@OptionsMenu(R.menu.menu_task_list)
@EActivity(R.layout.activity_navdrawer)
public class NavDrawerActivity extends RouteActivity {

    public static final String TASKLISTFRAGMENT = "tasklistfragment";
    public static final int PAUSE_OFFSET = 5; //offset for package-delivery time

    private User currentUser = User.findById(User.class, 1L);

    //public static final String ADDRESS_STARTPOINT = "Vallegata 10, Oslo";

    /**
     * two next bools to be used for showing drawer the first time app is started.
     */

    //used to estimate delivery time
    public List<Date> deliveryTime;
    public static NavDrawerActivity nda;

    //The Tasklist fragment
    private TaskListFragment taskListFragment;

    //Holds eventual Ad-Hoc tasks
    private Task adhocTask;

    @Extra
    Route extraRoute;

    @Bean
    DirectionsClient directionsClient;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.app_bar)
    Toolbar toolbar;

    @ViewById(R.id.drawer_list)
    RecyclerView recyclerView;

    /**
     * Sets actionbar of Activity
     * Defines drawer-behaviour
     */
    @AfterViews
    void setupDisplay() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavDrawerUtil.setupNavDrawer(this, toolbar, mDrawerLayout, recyclerView);

        this.route = extraRoute;
        this.dbRoute = route.getTask(0).getDbRoute();
        setTitle(dbRoute.getRouteName());

        /**
         * These are currently needed because we can't send SugarRecord id's as Extra in the object.
         * So we get the tasks again from DB from id's after sending it here.
         */
        List<Task> tasksConstructedFromIds = constructTasks(extraRoute.getTaskIds());
        this.route.setTasks(tasksConstructedFromIds);

        //TODO define transitionSet in xml
        TransitionSet transitionSet = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER).setDuration(1000);
        transitionSet.addTransition(new Slide(Gravity.LEFT).addTarget(findViewById(R.id.card_map)).setInterpolator(new AccelerateDecelerateInterpolator()));

        getWindow().getSharedElementEnterTransition().setDuration(1000).setInterpolator(new AccelerateDecelerateInterpolator());

        //initialises the fragment and puts it in the frame
        if (taskListFragment == null) {
            startFragment();
        }
        /**
         * Set username in drawer
         */
        TextView userName = (TextView) findViewById(R.id.profile_name);
        userName.setText(currentUser.getFirstname() + " " + currentUser.getLastname());

        nda = this;

        InstructionsUtil.checkVisited(this, "taskListVisited");
    }

    // Debug-mode
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(MainActivity.debugMode) getMenuInflater().inflate(R.menu.menu_task_list_debug, menu);
        else getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    void startFragment() {
        taskListFragment = no.mesan.handterminator.fragment.TaskListFragment_.builder().build();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.active_route_frame, taskListFragment, TASKLISTFRAGMENT)
                .commit();
    }

    public List<Date> getEstimates() {
        return deliveryTime;
    }

    public TaskListFragment getTaskListFragment() {
        return taskListFragment;
    }

    public void onTaskListClick(int index) {
        taskListFragment.mapLogic.onTaskListClick(index);
    }

    public void callReceiver()
    {
        Task current = taskListFragment.getRoute().getTask(taskListFragment.getCurrentTask());
        CommunicationUtil.callNumber(current.getReceiver().getPhone(), this);
    }

    /**
     * Clicking the FAB opens a delivery window for the current task
     * @param task Task to be sent to deliveryActivity.
     */
    public void goToDelivery(Task task) {
        final Intent intent = new Intent(this, DeliveryActivity_.class);

        findViewById(R.id.fragment_map).setVisibility(View.GONE);

        //shared element animation
        final ActivityOptions options =
                ActivityOptions.makeSceneTransitionAnimation(this,
                        Pair.create(findViewById(R.id.card_map), "leftTrans"),
                        Pair.create(findViewById(R.id.card_task_list), "midTrans"),
                        Pair.create(findViewById(R.id.card_camera_placeholder), "rightTrans"),
                        Pair.create(findViewById(R.id.deliverButton), "btnTrans")
                );

        intent.putExtra("id", task.getId());

        startDeliverActivity(intent, options);

    }

    private void startDeliverActivity(Intent intent, ActivityOptions options){
        ActivityCompat.startActivityForResult(this, intent, TaskListFragment.DELIVERED_RESULT, options.toBundle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TaskListFragment.DELIVERED_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                taskListFragment.finishTask();
                taskListFragment.mapLogic.centerMap();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // called from directions-service after finished route-calculation
    //@Override
    public void updateRoute(Route route) {
        boolean notFinished = partialOptimized;
        super.updateRoute(route);

        // Return if Google Directions is not done
        if(notFinished) return;

        //If adhocTask is added. Remove the temp task
        adhocTask = null;

        //Uppdate current active task to first unfinished
        taskListFragment.updateActive(0);

        //Oppdater rute
        taskListFragment.notifyMapUpdate(route);
    }

    @Override
    public void onBackPressed() {
        //if we're still delivering tasks. pause route, else end route on back.
        if(taskListFragment.getCurrentTask() < route.size())
            DialogUtil.makePauseRouteDialog(this);
        else
            DialogUtil.makeEndRouteDialog(this);
    }

    @Override
    protected void onResume() {
        findViewById(R.id.deliverButton).setClickable(true);
        findViewById(R.id.fragment_map).setVisibility(View.VISIBLE);
        taskListFragment.returnFromCard();
        super.onResume();
    }

    /**
     * Abomination of a method to get tasks from DB since we cant transfer it via extra.
     * @param ids List of Long id's for retrieving Tasks from DB
     * @return Tasks from DB
     */
    private List<Task> constructTasks(List<Long> ids){
        List<Task> tasks = new ArrayList<>();
        for(Long id : ids)
            tasks.add(Task.findById(Task.class, id));
        return tasks;
    }

    public void setProgressWheel(ProgressWheel wheel)
    {
        progressWheel = wheel;
    }


    /**
     * TEMP: Quickly deliver a task.
     */
    @OptionsItem(R.id.quickDeliver)
    void quickDeliver() {
        taskListFragment.finishTask();
    }

    public void notifyAdHoc(){
        adhocTask = new Mockup().createTask(new DBRoute(), -1);
        adhocTask.setType(Task.TASK_PICKUP);
        DialogUtil.showAdHoc(this, adhocTask);
    }

    public void acceptAdhoc(View view){
        taskListFragment.addTask(adhocTask);
        DialogUtil.hideAdHoc(this);
    }

    public void declineAdhoc(View view){
        DialogUtil.hideAdHoc(this);
        taskListFragment.dismissTask();
        adhocTask = null;
    }

    public void infoAdhoc(View view) {
        if (view.getId() == R.id.btn_adhoc_info) {
            AnimationUtil.crossFade(findViewById(R.id.adhoc_alert), findViewById(R.id.adhoc_info));
            taskListFragment.showTempRoute(adhocTask);
        } else {
            AnimationUtil.crossFade(findViewById(R.id.adhoc_info), findViewById(R.id.adhoc_alert));
            taskListFragment.showActualRoute();
        }
    }

    /**
     * Calculates the total statistics for the route after final package is delivered.
     */
    public void finalizeRouteStatistics() {
        long totalMeters = 0, totalSeconds = 0;
        double totalEarned = 0;

        for(Task task : this.route.getTasks()){
            long metersDriven = task.getDistance();
            long secondsSpent = task.getTime() + (PAUSE_OFFSET * 60);     //includes stops
            double moneyEarned = calculateMoneyEarned(task);
            totalMeters += metersDriven;
            totalEarned += moneyEarned;
            totalSeconds += secondsSpent;

        }

        //save route-statistics
        RouteStatistics routeStatistics = new RouteStatistics(dbRoute.getRouteName(), totalMeters, (long)totalEarned, totalSeconds, currentUser.getStatistics());
        routeStatistics.save();

    }

    /**
     * Calculate earnings from each package.
     * @param currentTask task to calculate
     */
    private double calculateMoneyEarned(Task currentTask){
        long secondsSpent = currentTask.getTime() + (NavDrawerActivity.PAUSE_OFFSET * 60);
        double hoursSpent = secondsSpent / 3600D;
        double totalTaskMoneyEarned = Statistics.calculateHourlyMoney(currentUser.getHourSalary(), hoursSpent);

        int numPackages = currentTask.getSize();
        totalTaskMoneyEarned += Statistics.calculatePackageMoney(currentUser.getPackageSalary(), numPackages);
        totalTaskMoneyEarned += Statistics.reimburseDriving((int)currentTask.getDistance());

        return totalTaskMoneyEarned;

    }
}
