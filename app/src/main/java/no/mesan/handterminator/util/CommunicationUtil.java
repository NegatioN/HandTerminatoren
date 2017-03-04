package no.mesan.handterminator.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

import no.mesan.handterminator.R;
import no.mesan.handterminator.model.db.Person;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;
import no.mesan.handterminator.service.SMSService;

/**
 * @author Martin Hagen, Sondre Sparby Boge
 */
public class CommunicationUtil {

    //filters used for sending sms
    private static final String SMS_SENT_INTENT_FILTER = "no.mesan.handterminator.sms_send";
    private static final String SMS_DELIVERED_INTENT_FILTER = "no.mesan.handterminator.sms_delivered";

    /**
     * called to start service
     * Service checks for deliveries with <= 20min to delivery
     *  - if it finds any, sends sms
     *  @param context
     */
    public static void startService(Context context) {
        Intent intent = new Intent();
        intent.setAction ("no.mesan.handterminator.mybroadcastreceiver");
        context.sendBroadcast(intent);
    }

    /**
     * Ends service
     * @param context
     */
    public static void stopService(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pSmsIntent = PendingIntent.getService(context, 0, new Intent(context, SMSService.class), 0);
        alarmManager.cancel(pSmsIntent);
    }

    /**
     * Call method, used for calling a set number(like to supervisor)
     * -calls callNumber(String number, Context context)
     * @param context
     */
    public static void call(Context context)
    {
        callNumber("12345678", context);
    }

    /**
     * Call method used
     * @param number
     * @param context
     */
    public static void callNumber(String number,Context context)
    {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+number));
        context.startActivity(callIntent);
    }

    /**
     * Sends an sms to the receiver, manually or by the service
     * Gives a message saying the delivery will be delivered in about 20min
     * @param context
     * @param task the task wo be delivered, used to get the correct name and number
     */
    public static void sendSMS(Context context, Task task)
    {
        //format of string is "package arrives in 20, " expetcs name of customer and driver afterwards.
        String message = context.getResources().getString(R.string.sms_default_text);
        Person reciever = task.getReceiver();
        message += reciever.getName();      //add customer-name
        String messagePartTwo = context.getResources().getString(R.string.sms_default_second_part);
        User currentUser = User.findById(User.class, 1L);

        message = (message + messagePartTwo + currentUser + ".");

        String phnNo = reciever.getPhone();

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(
                SMS_SENT_INTENT_FILTER), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(
                SMS_DELIVERED_INTENT_FILTER), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phnNo, null, message, sentPI, deliveredPI);
    }
}
