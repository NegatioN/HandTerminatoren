package no.mesan.handterminator;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.mesan.handterminator.adapter.DeliveryListAdapter;
import no.mesan.handterminator.fragment.CameraFragment;
import no.mesan.handterminator.fragment.DeliveryNoSignatureFragment;
import no.mesan.handterminator.fragment.DeliveryNoSignatureFragment_;
import no.mesan.handterminator.fragment.DeliverySignatureFragment;
import no.mesan.handterminator.fragment.DeliverySignatureFragment_;
import no.mesan.handterminator.model.db.Package;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;
import no.mesan.handterminator.util.AnimationUtil;
import no.mesan.handterminator.util.DialogUtil;
import no.mesan.handterminator.util.InstructionsUtil;
import no.mesan.handterminator.util.NavDrawerUtil;
import no.mesan.handterminator.util.ScanningUtil;

/**
 * @author Martin Hagen
 *         Activity that handles deliveries
 *         Packages are scanned out and the delivery is signed for in this activity
 *         - Also contains information and other alternatives(no signature)
 */
//@OptionsMenu(R.menu.menu_delivery)
@EActivity(R.layout.activity_delivery)
public class DeliveryActivity extends ActionBarActivity {

    private User currentUser = User.findById(User.class, 1L);

    public static final int DEVIATION_RESULT = 101;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.app_bar)
    Toolbar toolbar;

    @ViewById(R.id.drawer_list)
    RecyclerView recyclerView;

    //TaskList fields
    @ViewById(R.id.recyclerDelivery)
    RecyclerView deliveryRecycler;

    @ViewById(R.id.camera_layout_delivery)
    FrameLayout cameraLayout;

    @ViewById
    ImageButton continueButton;

    @ViewById
    ImageButton clearSignature;

    @ViewById(R.id.cancel_camera_button)
    ImageButton cancelCameraBtn;

    @ViewById(R.id.delivery_count)
    TextView total_tv;

    @ViewById
    View ripple;

    @ViewById(R.id.delivery_header)
    TextView title;

    @Extra
    long id;

    private DeliverySignatureFragment signatureFragment;
    private DeliveryNoSignatureFragment noSignatureFragment;
    private CameraFragment cameraFragment;

    private DeliveryListAdapter deliveryListAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Task task;

    private boolean signature = true;

    @AfterViews
    void setupDisplay() {
        setSupportActionBar(toolbar);

        task = Task.findById(Task.class, id);

        NavDrawerUtil.setupNavDrawer(this, toolbar, mDrawerLayout, recyclerView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupInfo();
        setUpList();

        //Make sure no packages are allready scanned
        deliveryListAdapter.unscanAll();

        //Set username in drawer
        TextView userName = (TextView) findViewById(R.id.profile_name);
        userName.setText(currentUser.getFirstname() + " " + currentUser.getLastname());

        TransitionSet transitionSet = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER).setDuration(1000).setInterpolator(new LinearInterpolator());
//        transitionSet.addTransition(new Slide(Gravity.RIGHT).addTarget(findViewById(R.id.card_delivery_scanner)));

        AnimationUtil.fadeView(findViewById(R.id.delivery_content), 1000, null);
        getWindow().getSharedElementEnterTransition().setDuration(1000).setInterpolator(new AccelerateDecelerateInterpolator());

        if (cameraFragment == null)
            cameraFragment = new CameraFragment();

        cameraLayout.setVisibility(View.INVISIBLE);
        startCamera();

        updateTotal();

        InstructionsUtil.checkVisited(this, "deliveryVisited");
    }

    // Debug-mode
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(MainActivity.debugMode) getMenuInflater().inflate(R.menu.menu_delivery_debug, menu);
        else getMenuInflater().inflate(R.menu.menu_delivery, menu);
        return true;
    }

    /**
     * Initializes TextVeiw's showed on screen
     * - uses info from selected task
     */
    public void setupInfo() {
        TextView id = (TextView) findViewById(R.id.delivery_id_field);
        TextView reciever = (TextView) findViewById(R.id.delivery_receiver_field);
        TextView address = (TextView) findViewById(R.id.delivery_address_field);
        TextView packages = (TextView) findViewById(R.id.delivery_package_field);

        title.setText(getString(R.string.field_delivery) + task.getName());
        id.setText(task.getId() + "");
        reciever.setText(task.getReceiver().getName());
        address.setText(task.getAddress());
        packages.setText(task.getPackages().size() + "");
    }

    /**
     * sets up RecyclerView(list) with correct values
     */
    public void setUpList() {
        List<Package> packages;

        packages = task.getPackages();

        deliveryListAdapter = new DeliveryListAdapter(this, packages);
        deliveryListAdapter.setHasStableIds(true);
        deliveryRecycler.setAdapter(deliveryListAdapter);

        layoutManager = new LinearLayoutManager(this);
        deliveryRecycler.setLayoutManager(layoutManager);
    }

    /**
     * Toggles camera, almost same as in AnimationUtil, but don't move fab
     * Uses animations from AnimationUtil, but customized to fit this activity
     */
    public void timedOut(boolean moveBtn) {
        final int radius = (continueButton.getRight() - continueButton.getLeft()) / 2;
        final float maxRadius = Math.max(ripple.getWidth(), ripple.getHeight());

        continueButton.setImageResource(R.drawable.ic_camera_alt_white_48dp);
        cancelCameraBtn.setVisibility(View.INVISIBLE);

        ripple.animate().alpha(0);
        ripple.setVisibility(View.VISIBLE);
        ripple.animate().alpha(1);

        if (moveBtn) {
            ViewPropertyAnimator anim = AnimationUtil.moveButtonToCenter(continueButton, ripple, radius, moveBtn);
            anim.start();
        }
        //fades camera out and shows signature-view
        AnimationUtil.circularFadeCamera(radius, maxRadius, ripple, cameraFragment, cameraLayout);

        continueButton.setVisibility(View.VISIBLE);
    }

    public void startCamera() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(cameraLayout.getId(), cameraFragment, "CAMERA")
                .commit();
    }

    /**
     * onClick for the fab on this activity
     * 2 states, center and corner
     * Center - starts camera(some animations)
     * Corner - Continue
     */
    @Click
    void continueButtonClicked() {
        if (continueButton.getX() == AnimationUtil.getMiddle(ripple, AnimationUtil.getRadius(continueButton))) {
            ripple.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.INVISIBLE);

            AnimationUtil.circularRevealCamera(continueButton, ripple, cameraFragment, cameraLayout, cancelCameraBtn);
            continueButton.setImageResource(R.drawable.ic_check);
        } else if (isDeliveryReady())
            DialogUtil.makeDeliveryDialog(this);
    }

    /**
     * Method called from button "inside" camera
     * lets user exit camera
     */
    @Click
    void cancel_camera_buttonClicked() {
        timedOut(false);
    }

    @Click
    void clear_signatureClicked(){
        if(signatureFragment != null)
            signatureFragment.clearSignature();
    }

    public void updateTotal() {
        int total = deliveryListAdapter.getList().size();
        int scanned = deliveryListAdapter.countScanned();

        total_tv.setText(scanned + "/" + total);
    }

    /**
     * Animates the transition when all packages are scanned and the signature fragment is to start
     * Uses animations from AnimationUtil, but customized to fit this activity
     * - Moves fab to corner for its second state
     */
    @OptionsItem(R.id.action_done_scanning)
    void allScanned() {
        deliveryListAdapter.scanAll();
        updateTotal();
        startSignature();
    }

    @OptionsItem(R.id.deviation_action)
    void reportDeviation() {
        // sets the animation and elements to exclude
        Transition explode = AnimationUtil.getExplodeTransition();

        Window window = getWindow();
        window.setEnterTransition(explode);

        //shared element animation
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, title, "login");

        Intent intent = new Intent(this, DeviationActivity_.class);
        intent.putExtra("taskId", task.getId());
        ActivityCompat.startActivityForResult(this, intent, DEVIATION_RESULT, options.toBundle());

    }

    @OptionsItem(R.id.action_no_signature)
    void noSignature(MenuItem item) {

        setItemTitle(item);

        //test if signature fragment is up
        // - if so, change fragment
        DeliverySignatureFragment signatureFrag =
                (DeliverySignatureFragment) getSupportFragmentManager().findFragmentByTag("SIGNATURE");
        DeliveryNoSignatureFragment nosignatureFrag =
                (DeliveryNoSignatureFragment) getSupportFragmentManager().findFragmentByTag("NOSIGNATURE");

        if (signatureFrag != null && signatureFrag.isVisible()) {
            clearSignature.setVisibility(View.GONE);
            noSignatureFragment = DeliveryNoSignatureFragment_.builder().build();
            replaceFragment(noSignatureFragment, "NOSIGNATURE");
        } else if (nosignatureFrag != null && nosignatureFrag.isVisible()) {
            clearSignature.setVisibility(View.VISIBLE);
            signatureFragment = DeliverySignatureFragment_.builder().build();
            replaceFragment(signatureFragment, "SIGNATURE");
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            signatureFragment.setTargetLocation(dateFormat.format(new Date()) + "_signature_id" + task.getId());
        }
    }

    @OptionsItem(R.id.action_manual_scan)
    void manualScanning() {
        //manualKolliDialig needs a list of tasks, for compatibility
        //makes a new list and adds active task
        List<Task> singleTaskList = new ArrayList<>();
        singleTaskList.add(task);
        ScanningUtil.manualKolliDialog(this, singleTaskList);
    }

    /**
     * Replaces active fragment. Used to switch from camera to signature fragment
     *
     * @param fragment - takes the next fragment in
     * @param name     - name of the fragment, used as name/id
     */
    public void replaceFragment(Fragment fragment, String name) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(cameraLayout.getId(), fragment, name)
                .commit();
    }

    /**
     * called from camera-fragment, scanned string as param
     * updates package list in the adapter
     *
     * @param scanned - input string
     */
    public void updateScanned(String scanned) {
        if (deliveryListAdapter.updateScannedPackage(scanned.trim())) {
            updateTotal();
            ScanningUtil.showScannedSnackBar(this, scanned.trim());
        }
    }

    /**
     * Method used for changing title in MenuItem
     * - gives user option to deliver with or without signature
     * also changes signature boolean
     *
     * @param item - MenuItem
     */
    public void setItemTitle(MenuItem item) {
        signature = !signature; // switches the boolean
        Log.d("Signature", "Signatur er: " + signature);
        //sets correct string in actionbar
        int stringId = (signature) ? R.string.action_no_signature : R.string.action_with_signature;
        if (!signature) item.setIcon(R.drawable.ic_deliver_unsigned);
        else item.setIcon(R.drawable.ic_deliver_signed);
        item.setTitle(stringId);
    }

    /**
     * Called from snackbar
     * - unscanns package
     */
    public void unscannPackage(String kolli) {
        int scanned = deliveryListAdapter.countScanned();

        boolean unscanned = deliveryListAdapter.unscannPackage(kolli);
        if (unscanned) {
            //before the unscanning, all packages was scanned
            if (scanned == deliveryListAdapter.getList().size()) {
                clearSignature.setVisibility(View.GONE);
                timedOut(true);
                replaceFragment(cameraFragment, "CAMERA");
            }
            updateTotal();
            Toast.makeText(this, "Innscanning fjernet", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Animates the transition when all packages are scanned and the signature fragment is to start
     * Uses animations from AnimationUtil, but customized to fit this activity
     * - Moves fab to corner for its second state
     */
    public void startSignature() {
        if (!cameraFragment.isStopped())
            timedOut(false);

        //if with signature is chosen, show signature fragment
        if (signature) {
            clearSignature.setVisibility(View.VISIBLE);
            signatureFragment = DeliverySignatureFragment_.builder().build();
            replaceFragment(signatureFragment, "SIGNATURE");
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            signatureFragment.setTargetLocation(dateFormat.format(new Date()) + "_signature_id" + task.getId());
        } else {//start no-signature fragment
            clearSignature.setVisibility(View.GONE);
            noSignatureFragment = DeliveryNoSignatureFragment_.builder().build();
            replaceFragment(noSignatureFragment, "NOSIGNATURE");
        }

        cameraLayout.setVisibility(View.VISIBLE);

        final int buttonMargin = (int) getResources().getDimension(R.dimen.add_button_margin);
        continueButton.setImageResource(R.drawable.ic_check);
        AnimationUtil.moveButtonToCorner(continueButton, ripple, AnimationUtil.getRadius(continueButton), buttonMargin, false);
        continueButton.setVisibility(View.VISIBLE);
    }

    /**
     * Complete the delivery of this task, save signature and go back to tasklist.
     */
    public void continueDelivery() {
        if (signature) {
            setResult(RESULT_OK);
            finishAfterTransition();
        } else {
            noSignatureFragment.saveDelivery();
            setResult(RESULT_OK);
            finishAfterTransition();
        }

    }

    /**
     * Check if the task is ready for delivery (Packages scanned and signature/comment added)
     * Display error message if not ready.
     *
     * @return bool - is the task ready for delivery?
     */
    public boolean isDeliveryReady() {
        if (signature && !signatureFragment.saveSignature()) {
            Toast.makeText(this, "Ikke godkjent signatur", Toast.LENGTH_LONG).show();
            return false;
        } else if (!signature && !noSignatureFragment.isUsed()) {
            Toast.makeText(this, "Ingen kommentar lagt til", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void deviationHandled() {
        Toast.makeText(this, "Avvik lagret", Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        finishAfterTransition();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEVIATION_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                deviationHandled();
            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (cameraFragment.isStopped()) {
            deliveryListAdapter.unscanAll();
            super.onBackPressed();
        }
        else
            timedOut(false);
    }

    public void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (!imm.getEnabledInputMethodList().isEmpty())
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}