package no.mesan.handterminator.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import no.mesan.handterminator.service.PeriodicService;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    /**
     * On receive this method starts the periodicservice intent. This class is just for starting the intent
     * @param context
     * @param intent
     */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

        Toast.makeText(context, "I MYBROADCASTRECEIVER", Toast.LENGTH_SHORT).show();
        Log.d(" MYBROADCASTRECEIVER", "!!!!!!!!!!!!!!!!!!!!!");
        Intent i = new Intent(context, PeriodicService.class);
		context.startService(i);
	}

}
