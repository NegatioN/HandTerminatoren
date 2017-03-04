package no.mesan.handterminator;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.List;

import no.mesan.handterminator.adapter.PackageListAdapter;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.util.DialogUtil;

@EActivity
public class CardActivity extends ActionBarActivity {

    private Toolbar cardToolbar;
    private String name;
    private List<no.mesan.handterminator.model.db.Package> packages;

    private RecyclerView packageList;

    private RecyclerView.LayoutManager layoutManager;
    private PackageListAdapter packageListAdapter;

    private Task selectedTask;

    @Extra
    long taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        packageList = (RecyclerView) findViewById(R.id.package_list);

        Window w = getWindow();
        Transition transition = new Fade();
        transition.setDuration(800);
        transition.setInterpolator(new LinearInterpolator());
        w.setExitTransition(transition);

        selectedTask = Task.findById(Task.class, taskId);


        name = selectedTask.getName();
        packages = selectedTask.getPackages();
        initiateValues(selectedTask);


        setUpList();

        cardToolbar =(Toolbar)findViewById(R.id.package_toolbar);
        modifyToolbar(cardToolbar);
    }

    @Override
    public void onBackPressed() {
        cardToolbar.getMenu().clear();
        finishAfterTransition();
        //super.onBackPressed();
    }

    private void setUpList() {

        packageListAdapter = new PackageListAdapter(this, packages);
        packageListAdapter.setHasStableIds(true);
        packageList.setAdapter(packageListAdapter);

        layoutManager = new LinearLayoutManager(this);
        packageList.setLayoutManager(layoutManager);
    }
    

    //Onclick for clicking outside the card. Closes the card.
    public void clickBack(View view){
        onBackPressed();
    }

    public void initiateValues(Task task)
    {

        //Set the right icon in the toolbar
        int icon;
        switch (task.getType()) {
            case Task.TASK_DELIVERY:
                icon = R.drawable.ic_delivery;
                break;
            case Task.TASK_PICKUP:
                icon = R.drawable.ic_pickup;
                break;
            default:
                icon = R.drawable.ic_delivery;
        }
        ((Toolbar)findViewById(R.id.package_toolbar)).setNavigationIcon(icon);

        //task fields
        TextView number = (TextView)findViewById(R.id.route_id_field);
        TextView type = (TextView)findViewById(R.id.task_type_field);
        TextView address = (TextView)findViewById(R.id.task_address_field);
        TextView timeslot = (TextView)findViewById(R.id.task_timeslot_field);

        number.setText(task.getId()+"");
        type.setText(((task.getType()==0)?"Levering":"Henting")+ " (" + task.getSize() + " kolli)");
        address.setText(task.getAddress() + ", " + task.getZip() + " " + task.getCity());
        long timeSlotStart = task.getTimeSlotStart();
        long timeSlotEnd = task.getTimeSlotEnd();
        //set timeslot for package if it exists
        if(timeSlotStart != 0 && timeSlotEnd != 0)
            timeslot.setText(task.getTimeSlotStartString() + " - " + task.getTimeSlotEndString());

        //sender fields
        TextView sender_name = (TextView)findViewById(R.id.sender_name_field);
        TextView sender_phone = (TextView)findViewById(R.id.sender_phone_field);
        TextView sender_address = (TextView)findViewById(R.id.sender_address_field);

        sender_name.setText(task.getSender().getName());
        sender_phone.setText(task.getSender().getPhone());
        sender_address.setText(task.getSender().getAddress());

        //receiver fields
        TextView receiver_name = (TextView)findViewById(R.id.receiver_name_field);
        TextView receiver_phone = (TextView)findViewById(R.id.receiver_phone_field);
        TextView receiver_address = (TextView)findViewById(R.id.receiver_address_field);

        receiver_name.setText(task.getReceiver().getName());
        receiver_phone.setText(task.getReceiver().getPhone());
        receiver_address.setText(task.getReceiver().getAddress());
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
        toolbar.setTitle(name);
        toolbar.inflateMenu(R.menu.menu_toolbar_package);

        //needed to send context to util classes
        final Context context = this;

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.action_exit_fragment:
                                onBackPressed();
                                break;
                            case R.id.action_phone:
                                DialogUtil.makePhoneDialog(context,R.string.dialog_phone_title, R.string.dialog_phone_customer_positive, selectedTask.getReceiver().getPhone());
                                break;
                            case R.id.action_sms:
                                DialogUtil.makeSMSDialog(context, selectedTask);
                                break;
                            default:break;
                        }
                        return true;
                    }
                });

    }


}
