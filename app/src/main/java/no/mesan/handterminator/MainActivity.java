package no.mesan.handterminator;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Package;
import no.mesan.handterminator.model.db.Person;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;
import no.mesan.handterminator.view.ProgressWheel;

@OptionsMenu(R.menu.menu_main)
@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity {

    private static final String FIRSTLAUNCH = "FIRST_LAUNCH";
    private boolean firstLaunch;
    @ViewById
    Button login;

    @ViewById
    EditText etUsername;

    @ViewById
    EditText etPassword;

    @ViewById
    TextView tvFeedback;

    public static MainActivity ma;

    public static boolean debugMode = false;

    @AfterViews
    void setup(){
        ViewCompat.setTransitionName(login, "login");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        firstLaunch = preferences.getBoolean(FIRSTLAUNCH, true);
        if(firstLaunch){
            new Mockup().generateDBRoutes();
            SharedPreferences.Editor prefEditor = preferences.edit();
            prefEditor.putBoolean(FIRSTLAUNCH, false).commit();
        }
        ma = this;
    }

    @Click
    void loginClicked() {
        String uName = etUsername.getText().toString();
        String pWord = etPassword.getText().toString();

        // no need to enter username/password in login
        if(uName.equals(""))
            RouteListActivity_.intent(this).start();
        else {

            User user = User.findById(User.class, 1L);

            if (uName.equals(user.getLogin())) {
                if (pWord.equals(user.getPassword()))
                    RouteListActivity_.intent(this).start();
                else {
                    etPassword.requestFocus();
                    showFeedback("Brukernavn eller passord er feil.");
                }
            } else {
                etPassword.setText("");
                etUsername.requestFocus();
                showFeedback("Brukernavn eller passord er feil.");
                mockupConsole(uName);
            }

        }
    }

    public void showFeedback(String text){
        tvFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setText(text);
    }

    // used as debug in development for quickly open activities and major functions
    private void mockupConsole(String input) {
        tvFeedback.setVisibility(View.INVISIBLE);
        etUsername.setText("");

        if(input.equals("clear")) {
            DBRoute.deleteAll(DBRoute.class);
            Package.deleteAll(Package.class);
            Person.deleteAll(Person.class);
            Task.deleteAll(Task.class);
            new Mockup().generateDBRoutes();
            Toast.makeText(this, "DB cleared", Toast.LENGTH_SHORT).show();
        }
        else if(input.equals("unscan"))
            for(Package p : Package.listAll(Package.class)){
                p.setScannedIn(false);
                p.setScannedOut(false);
            }
        else if(input.equals("print kolli")) {
            String out = "";
            for(Task t : Task.find(Task.class, "type = ?", "0")) for(Package p : t.getPackages()) out += "\n" + p.getKolli();
            Log.d("List of kolli", out);
        }
        else if(input.equalsIgnoreCase("debug")) {
            debugMode = !debugMode;
            Toast.makeText(this, "Debug-mode: " + ((debugMode) ? "On" : "Off"), Toast.LENGTH_SHORT ).show();
        }
        else tvFeedback.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        etUsername.setText("");
        etPassword.setText("");
        tvFeedback.setVisibility(View.INVISIBLE);
        super.onResume();
    }
}
