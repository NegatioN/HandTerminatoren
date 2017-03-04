package no.mesan.handterminator;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import no.mesan.handterminator.model.db.Deviation;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;

/**
 * @author Martin Hagen
 * This activity handles reporting of a Deviation for a certain Task.
 *
 * The activity resembles a dialog, with multiple input fields.
 */
@EActivity
public class DeviationActivity extends ActionBarActivity  {

    private User currentUser = User.findById(User.class, 1L);
    private Toolbar cardToolbar;

    @ViewById(R.id.deviation_spinner)
    Spinner deviationSpinner;

    @Extra
    long taskId;

    @ViewById(R.id.deviation_message_field)
    EditText commentField;

    private Task selectedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviation);

        Window w = getWindow();
        Transition transition = new Fade();
        transition.setDuration(800);
        transition.setInterpolator(new LinearInterpolator());
        w.setExitTransition(transition);

        selectedTask = Task.findById(Task.class, taskId);

        cardToolbar =(Toolbar)findViewById(R.id.package_toolbar);
        modifyToolbar(cardToolbar);

        // Create an adapter using the string array(R.id)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.deviations, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        deviationSpinner.setAdapter(adapter);

    }

    /**
     * onClick listener for the "rapporter"-button on the screen
     * Reads the different input fields ans creates a new Deviation with those values
     * Then save and exit
     */
    @Click
    void deviationButtonClicked() {
        //read input
        int deviationType = deviationSpinner.getSelectedItemPosition();
        String comment = commentField.getText().toString();

        Deviation deviation = new Deviation(comment, deviationType, selectedTask);
        deviation.save();

        //update statistics of user adding deviations
        currentUser.getStatistics().addDeviationReported();
        setResult(RESULT_OK);

        //exit
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        cardToolbar.getMenu().clear();

        finishAfterTransition();
    }
    //Onclick for clicking outside the card. Closes the card.
    public void clickBack(View view){
        onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * manually defines our toolbar's items and listener
     * @param toolbar the toolbar to be modified.
     */
    void modifyToolbar(Toolbar toolbar){
        toolbar.setTitle("Rapporter avvik: " + selectedTask.getName());
        toolbar.setTitleTextColor(getResources().getColor(R.color.primary));
        toolbar.inflateMenu(R.menu.menu_toolbar_exit);

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.action_exit_fragment:

                                onBackPressed();
                                break;
                            default:break;
                        }
                        return true;
                    }
                });

    }
}
