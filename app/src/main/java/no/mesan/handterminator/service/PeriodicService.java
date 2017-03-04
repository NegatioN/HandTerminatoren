package no.mesan.handterminator.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Calendar;


/**
 * @author Martin Hagen
 */
public class PeriodicService extends Service {

    /**
     * starts the SMSService periodically, according to the alarm set in this method
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

        //the intent this service is to start
		Intent i = new Intent(this, SMSService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, i, 0);

        Toast.makeText(getApplicationContext(), "I PERIODICSERVICE", Toast.LENGTH_SHORT).show();

        //Creates the alarm that controls the frequency of this service
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar timeStart = Calendar.getInstance();
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, timeStart.getTimeInMillis(),
				1000*60, pintent);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
