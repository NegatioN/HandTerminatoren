package no.mesan.handterminator.fragment;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import no.mesan.handterminator.NavDrawerActivity;
import no.mesan.handterminator.R;
import no.mesan.handterminator.RouteActivity;
import no.mesan.handterminator.adapter.TaskListAdapter;
import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.maps.Leg;
import no.mesan.handterminator.util.DialogUtil;
import no.mesan.handterminator.util.MapUtil;
import no.mesan.handterminator.util.maplogic.TaskListMapLogic;
import no.mesan.handterminator.view.ProgressWheel;
import no.mesan.handterminator.view.TaskRecyclerView;


/**
 * @author Lars-Erik Kasin, Sondre Sparby Boge
 *         <p/>
 *         Fragment for the TaskListActivity. Contains and initiates both map
 *         and taskList. Also updates map and list when notified of a route-change.
 */

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends Fragment {

    public static final int DELIVERED_RESULT = 101;

    // logic for showing and changing map and route
    public TaskListMapLogic mapLogic;

    // the map-object itself
    //@FragmentById(R.id.fragment_map)
    SupportMapFragment mapFragment;

    // map-header TextViews
    @ViewById
    TextView map_head_address, map_head_distance, map_head_time, map_head_progress;

    //TaskList fields
    @ViewById(R.id.recyclerTaskList)
    TaskRecyclerView taskListRecycler;

    private RecyclerView.LayoutManager layoutManager;
    private TaskListAdapter taskListAdapter;

    @ViewById
    TextView title;

    @ViewById
    ImageButton deliverButton;

    @ViewById(R.id.progress_wheel_task)
    ProgressWheel wheel;

    //Index of the currently active task in the taskList
    int currentTask;

    //Current route
    public Route route;


    /*
     * AfterView method for the fragment
     * This is where you initialise the fragment and create the list
     */
    @AfterViews
    void AddFragments() {
        route = ((RouteActivity) getActivity()).getRoute();

        //Generate TaskList (Recyclerview)
        setUpTaskList();

        //pass ProgressWheel to base class
        ((NavDrawerActivity) getActivity()).setProgressWheel(wheel);

        mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_map, mapFragment).commit();

        // initiate logic-class, with map-fragment, route and header-views as params
        mapLogic = new TaskListMapLogic(mapFragment, route, map_head_address, map_head_distance, map_head_time, map_head_progress);
    }


    /**
     * Set up the TaskList RecyclerView
     */
    private void setUpTaskList() {
        //Create and set adapter for the recyclerview
        taskListAdapter = new TaskListAdapter(getActivity(), route);
        taskListAdapter.setHasStableIds(true);
        taskListRecycler.setAdapter(taskListAdapter);
        taskListRecycler.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        taskListRecycler.setLayoutManager(layoutManager);

        //Deactivate the deliverbutton until animation and setup of elements are finished.
        deliverButton.setClickable(false);

        //Expand and activate the first task and enable the deliverButton after a delay
        //(to let animation and google calls finish)
        getView().postDelayed(new Runnable() {
            public void run() {
                initiateRoute();
                deliverButton.setClickable(true);
            }
        }, 1300);

    }

    /**
     * Sets the initial taskindex of the map and tasklist.
     */
    private void initiateRoute() {
        updateActive(currentTask);
        mapLogic.updateRoute(currentTask);
        generateTaskTimeslots(route.getTasks());
    }

    // syncs addresses for map-objects & tasklist-objects
    private void updateAddresses() {
        if (route == null) return;

        for (int i = 0; i < route.size(); i++) {
            Task t = route.getTask(i);
            Leg l = route.getLegs().get(i);

            Log.d("updateAdress", "Update address of task " + t.getName());
            t.setGoogleAddress(l.getEnd_address());
            t.setTime(l.getDuration().getValue());
            t.setDistance(l.getDistance().getValue());
        }
    }

    /**
     * notifies components interacting with the map
     *
     * @param route object received from Google Directions
     */
    public void notifyMapUpdate(Route route) {
        this.route = route;
        if (mapLogic == null) return;
        mapLogic.setRoute(route);

        taskListAdapter.estimateDeliveries(route, false);

        //Check if there are packages outside the given timeslots
        boolean timeWarning = false;
        for (Task t: route.getTasks())
            if (!t.isFinished() && !t.isWithinTimeslot()) {
                timeWarning = true;
                break;
            }

        if (timeWarning)
            DialogUtil.makeTimeslotWarning(getActivity());

        updateAddresses();
        mapLogic.updateMap(currentTask);
    }

    public int getCurrentTask() {
        return currentTask;
    }

    public void setRoute(Route route) {
        this.route = route;
        mapLogic.route = route;
    }

    public Route getRoute() {
        return route;
    }

    public void returnFromCard() {
        taskListAdapter.setAnimating(false);
    }

    //Set the isEditing flag, to enable editing functions.
    public void setEditing(boolean set) {
        taskListRecycler.setEditing(set, currentTask);
    }

    public void updateActive(int position){
        currentTask = taskListAdapter.updateActive(position);
    }

    /**
     * finish the currently active task and proceeds to the next one,
     * or finishes the route if all tasks are finished.
     */
    public void finishTask() {
        taskListAdapter.finishTask(currentTask);

        //Update the active task
        updateActive(++currentTask);

        //Go to next task if all tasks are not finished. Update map and times.
        if (currentTask < route.size()) {
            mapLogic.updateRoute(currentTask);

            taskListAdapter.estimateDeliveries(route, true);

            //Autoscroll to active task
            taskListRecycler.focusTask(currentTask);
        }//last delivery
        else if (currentTask == route.size()) {
            mapLogic.updateRoute(currentTask);
            taskListAdapter.notifyItemChanged(currentTask - 1);
            deliverButton.setImageResource(R.drawable.ic_arrow_forward_white_48dp);
        }

        if (currentTask == 2)
            deliverButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((NavDrawerActivity)getActivity()).notifyAdHoc();
                }
            }, 2000);
    }

    /**
     * Click on the deliverbutton while in edit mode.
     * Update the list after edit.
     */
    public void endEditing() {
        setEditing(false);

        //Delay the directions call to wait for button animation to finish
        getView().postDelayed(
                new Runnable() {
                    public void run() {
                        // use requestDirectionsUpdate for updating the route, using List<Task> as parameter
                        ((NavDrawerActivity) getActivity()).requestDirectionsUpdate(route, false);
                    }
                }, 320);
    }

    /**
     * Click on the deliverbutton while not in edit mode (Deliver package)
     */
    public void deliver() {
        if (currentTask < route.size()) {
            ((NavDrawerActivity) getActivity()).goToDelivery(taskListAdapter.getTask(currentTask));
            deliverButton.setClickable(false);
        }
        else {
            DialogUtil.makeEndRouteDialog((NavDrawerActivity)getActivity());
        }
    }


    @Click
    void deliverButtonClicked() {
        if (taskListRecycler.isEditing())
            endEditing();
        else
            deliver();
    }

    public void generateTaskTimeslots(List<Task> tasksList) {
        int timed = 2; // number of timed tasks

        // reset all time-slots
        for (int i = 0; i < tasksList.size(); i++) {
            tasksList.get(i).setPickupTimeStart(0);
            tasksList.get(i).setPickupTimeEnd(0);
        }

        // set timeslots for random tasks
        for (int i = 0; i < timed; i++) {
            int r = (new Random()).nextInt(tasksList.size());
            Task t = tasksList.get(r);

            //What amount of minutes should we round to? Closest 15 minutes/30minutes/whole hour?
            int step = 30;
            //Size of the timeslot in minutes
            int slotMinutes = 60;

            Date eta = t.getEta();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(eta);

            //Distance from closest step before estimated time
            int margin = calendar.get(Calendar.MINUTE) % step;
            //If closer than 5 minutes, add a full step margin at start
            margin += (margin < 5) ? step : 0;

            calendar.add(Calendar.MINUTE, -margin);
            t.setPickupTimeStart(calendar.getTimeInMillis());

            calendar.add(Calendar.MINUTE, slotMinutes);
            t.setPickupTimeEnd(calendar.getTimeInMillis());
        }

        // update task-timeframe
        for (Task t : tasksList) taskListAdapter.updateTimeFrame(t);
    }

    /**
     * Add an adhoc task to the route. Called from activity.
     *
     * @param task task to be added.
     */
    public void addTask(Task task) {
        route.addTask(task);
        taskListAdapter.addTask();
        currentTask = 0; //Reset the currentTask. Will find first unfinished.

        ((NavDrawerActivity) getActivity()).requestDirectionsUpdate(route, true);
    }

    public void dismissTask() {
        if (marker != null)
            marker.remove();
        marker = null;
    }

    private Marker marker;

    /**
     * Show temporary route with adhoc task, without adding this task to the route.
     *
     * @param task task to show.
     */
    public void showTempRoute(Task task) {
        marker = MapUtil.showTempMarker(task, mapFragment.getMap(), getActivity());
        MapUtil.expandCameraView(mapFragment.getMap(), route.getBounds(), marker);
    }

    /**
     * Show the real route after seeing a temporary adhoc route.
     */
    public void showActualRoute() {
        marker.remove();
        mapLogic.centerMap();
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (mapFragment != null) {
//            getFragmentManager().beginTransaction().remove(mapFragment).commit();
//        }
//    }
}
