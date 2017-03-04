package no.mesan.handterminator;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import no.mesan.handterminator.adapter.DriverListAdapter;
import no.mesan.handterminator.model.Driver;
import no.mesan.handterminator.util.AnimationUtil;
import no.mesan.handterminator.util.CommunicationUtil;
import no.mesan.handterminator.util.DialogUtil;


/**
 * @author Sondre Sparby Boge, Martin Hagen
 *
 * The emergency-card that shows up when the emergency-button in the drawer is clicked.
 * Only used to showcase intended functionality. Does not contain actual and finalized
 * functionality.
 */
@EActivity
public class EmergencyActivity extends ActionBarActivity {

    private Toolbar cardToolbar;

    @ViewById
    TextView headerEmergencyInfo;

    @ViewById(R.id.chose_driver_layout)
    RelativeLayout driverLayout;

    @ViewById(R.id.transfer_frame)
    RelativeLayout transferFrame;

    @ViewById(R.id.transfer_frame_2)
    RelativeLayout transferFrame2;

    @ViewById(R.id.deviation_frame)
    RelativeLayout deviationFrame;

    @ViewById(R.id.deviation_frame_2)
    RelativeLayout deviationFrame2;

    private int expandedRowHeight = 450;

    @ViewById(R.id.chosen_driver)
    TextView driverTV;

    @ViewById(R.id.transfer_status)
    TextView transferStatusTV;

    @ViewById(R.id.driver_list)
    RecyclerView driverList;

    @ViewById(R.id.choose_driver)
    Button driverBtn;

    @ViewById(R.id.call_driver)
    Button callDriverBtn;

    @ViewById(R.id.choose_driver_open)
    Button openDriver;

    @ViewById(R.id.transfer_button)
    Button transferButton;

    @ViewById(R.id.rapport_button)
    Button rapportButton;

    @ViewById(R.id.animation_package)
    ImageView animationPackage;

    @ViewById(R.id.deviation_comment)
    EditText deviationComment;

    private DriverListAdapter listAdapter;
    private RecyclerView.LayoutManager layoutManager;

    boolean transfered = false;

    int transfer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        setupGUI();
        setUpList();

        driverLayout.setVisibility(View.GONE);
        headerEmergencyInfo.setText("Velg sjåfør:");
    }

    public void setUpList()
    {
        ArrayList<Driver> list = new ArrayList<>();
        list.add(new Driver("Lucas Silva", "99988777", "< 5km"));
        list.add(new Driver("William Olsen","45000321","< 5km"));
        list.add(new Driver("Oddleif Olavsen","98456121","> 5km"));

        listAdapter = new DriverListAdapter(this, list);
        listAdapter.setHasStableIds(true);
        driverList.setAdapter(listAdapter);

        layoutManager = new LinearLayoutManager(this);
        driverList.setLayoutManager(layoutManager);
    }

    private void setupGUI() {
        cardToolbar =(Toolbar)findViewById(R.id.emergency_toolbar);
        modifyToolbar(cardToolbar);

        // Listens for enter-key click and hides keyboard
        deviationComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(deviationComment.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        transferPackage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Click
    void chooseDriver() {
        fadeChoseDriver();
        Driver selected = listAdapter.getDriver();
        if(selected != null) {
            driverTV.setText(selected.getName());
            transferStatusTV.setText("Klar for overføring til: " + selected.getName());
            AnimationUtil.crossFade(null, transferButton);
        }
        else
            openDriver.setVisibility(View.VISIBLE);
    }

    void fadeChoseDriver()
    {
        ValueAnimator valueAnimator;
        valueAnimator = ValueAnimator.ofInt(expandedRowHeight, 30);
        valueAnimator = AnimationUtil.taskExpandAnimator(valueAnimator, driverLayout);
        valueAnimator.start();
        AnimationUtil.fadeView(driverLayout, AnimationUtil.EXPAND_DURATION, new Runnable() {
            @Override
            public void run() {
                driverLayout.setVisibility(View.GONE);
                driverLayout.animate().alpha(1);
            }
        });
    }

    @Click
    void chooseDriverOpen() {
        ValueAnimator valueAnimator;
        driverLayout.setVisibility(View.VISIBLE);
        valueAnimator = ValueAnimator.ofInt(30, expandedRowHeight);
        valueAnimator = AnimationUtil.taskExpandAnimator(valueAnimator, driverLayout);
        valueAnimator.start();

        driverTV.setVisibility(View.VISIBLE);
        openDriver.setVisibility(View.GONE);
    }

    @Click
    void callDriver() {
        Driver selectedDriver = listAdapter.getDriver();
        if(selectedDriver != null)
            DialogUtil.makePhoneDialog(this, R.string.dialog_title_call_driver, R.string.dialog_phone_driver, selectedDriver.getNumber() );

    }

    @Click
    void transferButton()
    {
        transferButton.setEnabled(false);
        if(!transfered) {
            if (transfer == 3) {
                transfered = true;
                animationPackage.setVisibility(View.GONE);
                transferStatusTV.setText("Overføring fullført!");
                return;
            }
            animationPackage.setVisibility(View.VISIBLE);
            String[] dots = {".", "..", "..."};
            transferStatusTV.setText("Overføring pågår" + dots[transfer++]);

            animationPackage.animate().translationX(820).setDuration(1500).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animationPackage.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animationPackage.setTranslationX(0);
                            transferButton();
                        }
                    }, 500);
                }
            });
        }
    }

    @Click
    void rapportButtonClicked()
    {
        deviationComment.setText("");
        Toast.makeText(this, "Avvik lagret", Toast.LENGTH_LONG).show();
    }

    public void callToDriver()
    {
        Driver selected = listAdapter.getDriver();
        CommunicationUtil.callNumber(selected.getNumber(), this);
    }

    /**
     * manually defines our toolbar's items and listener
     * @param toolbar the toolbar to be modified.
     */
    void modifyToolbar(Toolbar toolbar){
        toolbar.setTitle("Nødsituasjon");
        toolbar.inflateMenu(R.menu.menu_toolbar_emergency);

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_transfer_package:
                                transferPackage();
                                break;
                            case R.id.action_rapport_damage:
                                rapportDamage();
                                break;
                            case R.id.action_exit_fragment:
                                onBackPressed();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
    }

    //Onclick for clicking outside the card. Closes the card.
    public void clickBack(View view){
        onBackPressed();
    }

    public void transferPackage() {
        driverLayout.setVisibility(View.GONE);

        transferFrame.setVisibility(View.VISIBLE);

        transferFrame2.setVisibility(View.VISIBLE);
        deviationFrame.setVisibility(View.GONE);

        deviationFrame2.setVisibility(View.GONE);

        driverLayout.setVisibility(View.GONE);
        if(driverTV.getText().equals("")) {
            openDriver.setVisibility(View.VISIBLE);
        }

    }

    public void rapportDamage() {

        driverLayout.setVisibility(View.GONE);

        transferFrame.setVisibility(View.GONE);

        transferFrame2.setVisibility(View.GONE);
        deviationFrame.setVisibility(View.VISIBLE);

        deviationFrame2.setVisibility(View.VISIBLE);

    }
}
