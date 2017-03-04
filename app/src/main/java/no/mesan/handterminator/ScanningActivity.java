package no.mesan.handterminator;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionSet;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.adapter.ScanListAdapter;
import no.mesan.handterminator.fragment.CameraFragment;
import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;
import no.mesan.handterminator.util.AnimationUtil;
import no.mesan.handterminator.util.ImageUtil;
import no.mesan.handterminator.util.InstructionsUtil;
import no.mesan.handterminator.util.NavDrawerUtil;
import no.mesan.handterminator.util.ScanningUtil;
import no.mesan.handterminator.view.ProgressWheel;

/**
 * Created by marhag on 25.02.2015.
 */
//@OptionsMenu(R.menu.menu_scanning)
@EActivity(R.layout.activity_scanning)
public class ScanningActivity extends RouteActivity {

    private static final double SCANNING_LIMIT = 0.90;

    private User currentUser = User.findById(User.class, 1L);

    private ActionBarDrawerToggle mDrawerToggle;
    /**
     * two next bools to be used for showing drawer the first time app is started.
     */
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;

    private AlertDialog alertManuelInput;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.app_bar)
    Toolbar toolbar;

    @ViewById(R.id.drawer_list)
    RecyclerView recyclerView;

    @ViewById(R.id.recyclerScanList)
    RecyclerView scanListRecycler;

    @ViewById(R.id.cameraButton)
    ImageButton cameraButton;

    @ViewById(R.id.cancel_camera_btn)
    ImageButton cancelCameraBtn;

    @ViewById(R.id.camera_frame)
    RelativeLayout cameraFrame;

    @ViewById
    View ripple;

    @Extra
    long selectedRouteId;

    private ScanListAdapter scanListAdapter;

    private CameraFragment cameraFragment;

    @ViewById(R.id.camera_layout)
    FrameLayout cameraLayout;

    @ViewById(R.id.progress_wheel_scanning)
    ProgressWheel wheel;

    /**
     * Sets actionbar of Activity
     * Defines drawer-behaviour
     */
    @AfterViews
    void setupDisplay(){
        setSupportActionBar(toolbar);

        NavDrawerUtil.setupNavDrawer(this, toolbar, mDrawerLayout, recyclerView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TransitionSet transitionSet = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER).setDuration(1000);
        transitionSet.addTransition(AnimationUtil.getSlide(Gravity.RIGHT, findViewById(R.id.card_scanner)));
        transitionSet.addTransition(AnimationUtil.getSlide(Gravity.RIGHT, findViewById(R.id.card_scan_list)));

        getWindow().setEnterTransition(transitionSet);
        getWindow().setReturnTransition(transitionSet);

        scanListAdapter = new ScanListAdapter(this,cameraButton, selectedRouteId, ripple);
        scanListAdapter.setHasStableIds(true);
        scanListRecycler.setAdapter(scanListAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        scanListRecycler.setLayoutManager(layoutManager);

        TextView userName = (TextView) findViewById(R.id.profile_name);
        userName.setText(currentUser.getFirstname() + " " + currentUser.getLastname());

        if (cameraFragment == null)
            cameraFragment = new CameraFragment();

        if (scanListAdapter.getPackagesScanned() > SCANNING_LIMIT) {
            cameraButton.setImageResource(ImageUtil.getButtonDrawable(scanListAdapter.getPackagesScanned()));
            AnimationUtil.setStartPosCorner(cameraButton,this, ripple);
        }

        cameraLayout.setVisibility(View.INVISIBLE);
        startCamera();

        InstructionsUtil.checkVisited(this, "scanningVisited");

        progressWheel = wheel;
    }

    // Debug-mode
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(MainActivity.debugMode) getMenuInflater().inflate(R.menu.menu_scanning_debug, menu);
        else getMenuInflater().inflate(R.menu.menu_scanning, menu);
        return true;
    }

    /**
     * Handles clicks on fab
     * If fab is in the middle of the screen and less scanned than limit, open camera
     * else, go to next screen
     */
    @Click
    void cameraButtonClicked(){
        double percentScanned = scanListAdapter.getPackagesScanned();
        final int radius = AnimationUtil.getRadius(cameraButton);

        //if FAB is in the middle, circular reveal camera and move button away
        if (AnimationUtil.inMiddle(ripple, cameraButton) && percentScanned < SCANNING_LIMIT) {
            AnimationUtil.animateButton(cameraButton, ripple, this, cameraFragment, cameraLayout, radius, percentScanned, cancelCameraBtn);
        }
        else
        {
            if(percentScanned >= SCANNING_LIMIT) {
                cameraButton.setClickable(false);
                startRoute();
            }
            else
            {
                Toast.makeText(this, "Ikke godkjent innscanning.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method called from button "in" camera
     * lets user exit camera
     */
    @Click
    void cancel_camera_btnClicked()
    {
        timedOut();
    }

    public void timedOut()
    {
        boolean moveBtn = true;
        if(scanListAdapter.getPackagesScanned() >= SCANNING_LIMIT)
            moveBtn = false;
        cameraButton.setVisibility(View.VISIBLE);
        cancelCameraBtn.setVisibility(View.INVISIBLE);
        AnimationUtil.cameraTimeout(cameraButton, ripple, cameraFragment, cameraLayout, moveBtn);
    }

    public void startRoute() {

        if (!cameraFragment.isStopped()) {
            timedOut();
        }

        findViewById(R.id.camera_frame).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (route == null) {
                    requestDirectionsUpdate(selectedRouteId, true);
                } else
                    updateRoute(route);
            }
        }, 1000);

    }

    // Shows an input-dialog with auto-completion for manual input of kolli
    @OptionsItem(R.id.manual_kolli_input)
    void manualKolli() {
        ScanningUtil.manualKolliDialog(this, scanListAdapter.getTaskList());

    }

    @OptionsItem(R.id.scan_all_kolli)
    void scanAll(){
        scanListAdapter.scanAll();
        DBRoute.findById(DBRoute.class, selectedRouteId).setStatus("Scannet inn");
        AnimationUtil.setStartPosCorner(cameraButton, this, ripple);
    }

    /**
     * sets camerafragment in scanningactivity's framelayout
     * this fragment is always active, but the camera is closed/opened on button click
     */
    public void startCamera()
    {
        getSupportFragmentManager()
                .beginTransaction()
                .add(cameraLayout.getId(), cameraFragment, "CAMERA")
                .commit();
    }

    /**
     * Not in juse, but clears framelayout
     */
    public void removeCamera() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(cameraFragment)
                .commit();
    }

    //Method called from directionsClient. Update this class' route object and open the tasklistactivity
    @Override
    public void updateRoute(Route route) {
        boolean notFinished = partialOptimized;
        super.updateRoute(route);

        // Return if Google Directions is not done
        if(notFinished) return;

        //shared element animation
        ActivityOptions options =
                ActivityOptions.makeSceneTransitionAnimation(this,
                        Pair.create(findViewById(R.id.map_fragment_placeholder), "leftTrans"),
                        Pair.create(findViewById(R.id.card_scan_list), "midTrans"),
                        Pair.create(findViewById(R.id.card_scanner), "rightTrans"),
                        Pair.create(findViewById(R.id.cameraButton), "buttonTrans")
                        );

        Intent intent = new Intent(this, NavDrawerActivity_.class);

        /**
         * These are currently needed because we can't send SugarRecord id's as Extra in the object.
         * So we get the tasks again from DB from id's after sending it to NavDrawerActivity
         */
        List<Long> taskIds = new ArrayList<>();
        for(Task task : route.getTasks()){
            taskIds.add(task.getId());
        }
        route.setTaskIds(taskIds);
        intent.putExtra("extraRoute", route);

        ActivityCompat.startActivity(this, intent, options.toBundle());
        setResult(RESULT_OK);
        finish();
    }

    // called from camera-fragment, scanned string as param
    public void updateScanned(String scanned) {
        if(scanListAdapter.updateScannedPackage(scanned.trim())) {
            DBRoute route = DBRoute.findById(DBRoute.class, selectedRouteId);
            if(scanListAdapter.getPackagesScanned() > SCANNING_LIMIT)
                route.setStatus("Scannet inn");
            else{
                if(!route.getStatus().equals("Scanning påbegynt"))
                    route.setStatus("Scanning påbegynt");
            }
            ScanningUtil.showScannedSnackBar(this, scanned.trim());
        }
    }

    /**
     * Called from snackbar
     * - unscanns package
     */
    public void unscannPackage(String kolli)
    {
        boolean unscanned = scanListAdapter.unscannPackage(kolli);
        double scanned = scanListAdapter.getPackagesScanned();
        if(unscanned)
            if(scanned < SCANNING_LIMIT) {
                DBRoute.findById(DBRoute.class, selectedRouteId).setStatus("Ikke påbegynt");
                timedOut();
            }
            Toast.makeText(this, "Innscanning fjernet", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        cameraButton.setClickable(true);
        scanListAdapter.getTaskList();
        cameraFragment.stopCamera();
        super.onResume();
    }

    private void toggleCamera() {
        if(cameraFragment.isStopped()) {
            cameraFragment.startCamera();
        }
        else {
            cameraFragment.stopCamera();
        }
    }

    @Override
    public void onBackPressed() {
        getWindow().setTransitionBackgroundFadeDuration(0);
        super.onBackPressed();
    }
}
