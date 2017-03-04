    package no.mesan.handterminator.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.mesan.handterminator.NavDrawerActivity;
import no.mesan.handterminator.model.db.Task;


    /**
     * @author Martin Hagen
     */
public class SMSService extends Service {
    private static final String SMS_SENT_INTENT_FILTER = "no.mesan.handterminator.sms_send";
    private static final String SMS_DELIVERED_INTENT_FILTER = "no.mesan.handterminator.sms_delivered";
    static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs

    NavDrawerActivity listContext;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * The method that finds task that are close to being delivered.
     * Also handles the notifications if we want
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent2= new Intent(getApplicationContext(), NavDrawerActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent2, 0);

        //this test has to be done to check if there is any tasks that are 20min < from delivery
        //the method can return a list of tasks that meet this criteria
        List<Task> checkedTasks = lookForTasks();
        if(checkedTasks.size()>0) {
            for(Task task : checkedTasks)
            {
                //sends the sms
                sendSms(task);
                /*
                 * if we want to send a notification after sending the sms, comment out this
                //start notification
                Notification noti = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Send").
                        setContentText("Send").setContentIntent(pIntent).setAutoCancel(true).
                        setSmallIcon(R.drawable.ic_drawer).build();
                noti.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

                notificationManager.notify(0, noti);*/
            }
        }
		return super.onStartCommand(intent, flags, startId);
	}

    /**
     * Metode that preforms the sending of the sms for each task
     * @param task - one delivery, contains the info used in the message
     */
    public void sendSms(Task task)
    {
        String message = "Pakke kommer om 20 min";

        String phnNo = "11119999";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                SMS_SENT_INTENT_FILTER), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(
                SMS_DELIVERED_INTENT_FILTER), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phnNo, null, message, sentPI, deliveredPI);
    }

    /**
     * Test that goes through all the coming tasks
     * The method get the current task list, and the estimated delivery list
     *
     * if a task has its delivery time between now and +20min it adds this task to the return list
     * if the task is added to the list, an boolean is set to true(is sent) so it wont set the same
     * sms twice
     * @return
     */
    public List<Task> lookForTasks()
    {
        listContext = NavDrawerActivity.nda;

        List<Task> tasks,list = new ArrayList<>();
        tasks = listContext.getTaskListFragment().getRoute().getTasks();
        List<Date> deliveries = listContext.getEstimates();
        int currentTask = listContext.getTaskListFragment().getCurrentTask();

        int i = 0;
        Date now = new Date();

        for(Task task:tasks)
        {
            if(i >= currentTask){
                Date delivery = deliveries.get(i);
                if(task.getEta().after(now) && task.getEta().before(new Date(now.getTime() + (20 * ONE_MINUTE_IN_MILLIS))))
                    if(!task.getSentSMS()) {
                        list.add(task);
                        task.setSentSMS(true);
                    }
            }
            i++;
        }

        return list;
    }

}
