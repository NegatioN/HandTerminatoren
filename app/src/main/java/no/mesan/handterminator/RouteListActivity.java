package no.mesan.handterminator;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.adapter.RouteListAdapter;
import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;
import no.mesan.handterminator.model.maps.Leg;
import no.mesan.handterminator.util.AnimationUtil;
import no.mesan.handterminator.util.InstructionsUtil;
import no.mesan.handterminator.util.MapUtil;
import no.mesan.handterminator.util.NavDrawerUtil;
import no.mesan.handterminator.util.PrintUtil;
import no.mesan.handterminator.util.maplogic.RouteListMapLogic;
import no.mesan.handterminator.view.ProgressWheel;

@OptionsMenu(R.menu.menu_route_list)
@EActivity(R.layout.activity_route_list)
public class RouteListActivity extends RouteActivity {

    public static final int FROM_ROUTE_RESULT = 100;

    public boolean test = true;

    List<DBRoute> routeList;

    private RouteListAdapter routeListAdapter;

    private User currentUser = User.findById(User.class, 1L);

    private static long selectedRouteId; //selected route in the routeList

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.app_bar)
    Toolbar toolbar;

    @ViewById(R.id.drawer_list)
    RecyclerView recyclerView;

    @ViewById
    TextView routeId, routeDeliveries, routePickups, routeDistance, routeTime;

    @ViewById(R.id.route_status_field)
    TextView routeStatus;

    @ViewById
    ImageView routeStamp;

    @ViewById(R.id.recyclerRouteList)
    RecyclerView routeListRecycler;

    @ViewById(R.id.progress_wheel_route)
    ProgressWheel wheel;

    private RouteListMapLogic mapLogic;

    @FragmentById(R.id.fragment_map_route_list)
    SupportMapFragment mapFragment;


    @AfterViews
    void setupDisplay() {
        setSupportActionBar(toolbar);

        selectedRouteId = 0;

        TextView userName = (TextView) findViewById(R.id.profile_name);
        userName.setText(currentUser.getFirstname() + " " + currentUser.getLastname());

        NavDrawerUtil.setupNavDrawer(this, toolbar, mDrawerLayout, recyclerView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_map_route_list, mapFragment).commit();

        /**
         * Get all routes from DB
         */

        routeList = new ArrayList<>();
        List<DBRoute> routesFromDb = DBRoute.listAll(DBRoute.class);
        routeList.addAll(routesFromDb);

        //sets up all DBRoutes in list of routes
        setUpList();

        //Initialize map
        mapLogic = new RouteListMapLogic(mapFragment, route);

        InstructionsUtil.checkVisited(this, "routeListVisited");

        progressWheel = wheel;
    }

    //Set up the routelist on the left side
    void setUpList() {
        routeListAdapter = new RouteListAdapter(this, routeList);

        routeListRecycler.setAdapter(routeListAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        routeListRecycler.setLayoutManager(layoutManager);
    }

    /**
     * Onclick method for clicks on the FAB
     */
    @Click
    void continueButtonClicked() {
        //sends the selected route-id to be retrieved from DB in scanningActivity.
        findViewById(R.id.continueButton).setClickable(false);

        DBRoute currentRoute = DBRoute.findById(DBRoute.class, selectedRouteId);

        // ignore fab-click if route is already completed
        if(currentRoute.isCompleted()) return;

        //
        setScanListTransition();
        boolean allPacksScanned = currentRoute.isAcceptableScanned();
        if(allPacksScanned){
            Intent intent = NavDrawerActivity_.intent(this).extra("selectedRouteId", selectedRouteId).get();
            ActivityCompat.startActivityForResult(this, intent, FROM_ROUTE_RESULT, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        }else {
            Intent intent = ScanningActivity_.intent(this).extra("selectedRouteId", selectedRouteId).get();
            ActivityCompat.startActivityForResult(this, intent, FROM_ROUTE_RESULT, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        }
    }

    public void removeViews(View... views){
        for (View view : views){
            view.setVisibility(View.GONE);
        }
    }

    /**
     * Counting the amount of deliveries (not pickups) in the route
     * @param tasks List of tasks to be checked
     * @return returns the number of deliveries
     */
    public int countDeliveries(List<Task> tasks) {
        int deliveryCount = 0;
        for (Task t : tasks)
            if (t.getType() == Task.TASK_DELIVERY)
                deliveryCount++;

        return deliveryCount;
    }

    /**
     * Swaps between placeholders and routeinfo in the details view,
     * depending on whether a route is chosen
     */
    public void swapPlaceholders(){
        View info = findViewById(R.id.route_info);

        //Hide detailsview and show the placeholder if a route was previously shown
        if (info.getVisibility() == View.GONE) {
            findViewById(R.id.placholder_route_info).setVisibility(View.GONE);
            info.setVisibility(View.VISIBLE);
        }
        else if (selectedRouteId == 0){
            findViewById(R.id.placholder_route_info).setVisibility(View.VISIBLE);
            info.setVisibility(View.GONE);
            findViewById(R.id.continueButton).setVisibility(View.GONE);
        }

    }

    /**
     * TODO: Is this viable?
     * Reset the entire view, returning all elements to it original state. (No route chosen)
     */
    public void resetView(){
        RouteListActivity_.intent(this).start();
        finish();
    }

    @Override
    public void updateRoute(Route route) {
        DBRoute currentRoute = dbRoute;
        if(currentRoute != null) {

            currentRoute.setBounds(route.getBounds());
            List<Leg> legs = route.getLegs();

            //estimate time and distance
            currentRoute.setDistance(legs);
            currentRoute.setEstTime(legs);

            //save latitude and longitude on Tasks
            int i = 0;
            for (Task task : currentRoute.getTasks()) {
                MapUtil.saveTaskPoint(i++, task, legs);
            }
            currentRoute.setgDirectionsCalled(true);
        }

        setUpList();
        showClickedRouteInfo(currentRoute);

        if (mapLogic != null) {
            mapLogic.setRoute(route);
            mapLogic.updateMap(dbRoute);
        }

        isCalculating = false;
        progressWheel.stopSpinning();
    }

    public void setSelectedRouteId(long id){
        selectedRouteId = id;
    }

    public static long getSelectedRouteId() {
        return selectedRouteId;
    }

    /**
     * checks if fab is visible, then starts selectRoute()
     * @param clickedRoute the clicked route - only passed to next method
     */
    public void routeListClicked(final DBRoute clickedRoute){
        ImageButton continueButton = (ImageButton) findViewById(R.id.continueButton);

        if (continueButton.getVisibility() != View.VISIBLE && !clickedRoute.isCompleted()) {
            if (!clickedRoute.isgDirectionsCalled()) {
                continueButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        selectRoute(clickedRoute);
                    }
                }, AnimationUtil.slideView(continueButton, this));

                return;
            }

            AnimationUtil.slideView(continueButton, this);
        }
        else if (clickedRoute.isCompleted() && continueButton.getVisibility() == View.VISIBLE)
            AnimationUtil.slideOutView(continueButton, this);

        selectRoute(clickedRoute);

    }

    /**
     * Respond to clicked route in the routelist. Update map and details views.
     * @param clickedRoute the clicked route
     */
    public void selectRoute(DBRoute clickedRoute)
    {
        //Set the selected route id, so this can be passed on to the next activity
        setSelectedRouteId(clickedRoute.getId());
        getClickedRouteInfo(clickedRoute);
        route = new Route(clickedRoute);
        dbRoute = clickedRoute;
    }

    /**
     * Load the clicked route's info from Google or DB before showing it onscreen.
     * @param dbRoute
     */
    public void getClickedRouteInfo(DBRoute dbRoute){
        if(!dbRoute.isgDirectionsCalled()) {
            //Update the map from Google Directions
            requestDirectionsUpdate(dbRoute.getId(), true);
        }
        else{
            //update map from DB
            if (mapLogic != null) {
                mapLogic.updateMap(dbRoute);
                //also update route status
                routeStatus.setText(dbRoute.getStatus());
            }

            showClickedRouteInfo(dbRoute);
        }
    }

    /**
     * Display details for given route on the right side of the screen.
     * @param dbRoute DBRoute to be displayed
     */
    public void showClickedRouteInfo(DBRoute dbRoute) {
        swapPlaceholders();

        List<Task> tasks = dbRoute.getTasks();

        //This is the most legitest math up in this whole shebang!
        long generatedId = dbRoute.getId() * (countDeliveries(tasks)^dbRoute.getDistance());

        //Update each field with info from the route
        routeId.setText(generatedId + "");
        routeDeliveries.setText(countDeliveries(tasks) + "");
        routePickups.setText(tasks.size() - countDeliveries(tasks) + "");
        routeDistance.setText(PrintUtil.displayDistance(dbRoute.getDistance()));
        routeTime.setText(PrintUtil.displayTime(dbRoute.getEstTime()));
        routeStatus.setText(dbRoute.getStatus());
        if (dbRoute.isCompleted()) {
            routeStamp.setVisibility(View.VISIBLE);
            mapFragment.getView().setAlpha(0.7f);
        }
        else {
            routeStamp.setVisibility(View.GONE);
            mapFragment.getView().setAlpha(1f);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FROM_ROUTE_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                resetView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Define transitions in and out of ScanListActivity
     */
    private void setScanListTransition(){
        TransitionSet transitionSet = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER).setDuration(1000);
        transitionSet.addTransition(AnimationUtil.getSlide(Gravity.LEFT, findViewById(R.id.card_route_details)));
        transitionSet.addTransition(AnimationUtil.getSlide(Gravity.LEFT, findViewById(R.id.card_route_list)));

        getWindow().setExitTransition(transitionSet);
        getWindow().setReenterTransition(transitionSet);
    }

    /**
     * Sets transition to null, so we can still see current activity behind our selected window
     * from the navDrawer
     */
    public void setDrawerClickTransition(){
        getWindow().setExitTransition(null);
        getWindow().setReenterTransition(null);
    }

    @Override
    protected void onResume() {
        findViewById(R.id.continueButton).setClickable(true);

        routeList = DBRoute.listAll(DBRoute.class);
        routeListAdapter.setRouteList(routeList);
        routeListAdapter.notifyDataSetChanged();

        //also update route status
        if(selectedRouteId != 0)
            showClickedRouteInfo(DBRoute.findById(DBRoute.class, selectedRouteId));
        super.onResume();
    }
}