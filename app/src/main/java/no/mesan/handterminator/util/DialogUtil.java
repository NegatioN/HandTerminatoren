package no.mesan.handterminator.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import no.mesan.handterminator.DeliveryActivity;
import no.mesan.handterminator.NavDrawerActivity;
import no.mesan.handterminator.R;
import no.mesan.handterminator.model.db.Task;

/**
 * @author Martin Hagen, Sondre Sparby Boge
 */
public class DialogUtil {


    /**
     * private helper-method used for creating a dialog. caller must set positive-button by itself.
     *
     * @param context context
     * @param title   titte of the dialog.
     * @return
     */
    private static AlertDialog.Builder makeDialog(final Context context, final int title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        return builder;
    }

    /**
     * Dialog for endRoute
     *
     * @param navDrawerActivity the context to display and compute in.
     */
    public static void makeEndRouteDialog(final NavDrawerActivity navDrawerActivity) {
        final AlertDialog.Builder alertDialogBuilder = makeDialog(navDrawerActivity, R.string.end_route);
        alertDialogBuilder.setPositiveButton(R.string.dialog_end_route_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                navDrawerActivity.finalizeRouteStatistics();
                navDrawerActivity.finish();
            }
        });

        //display smsDialog
        AlertDialog endRouteDialog = alertDialogBuilder.create();
        endRouteDialog.show();

        //customize title and button colors (to fit our theme)
        setColors(navDrawerActivity, endRouteDialog);
    }

    /**
     * Dialog for pauseRoute
     *
     * @param navDrawerActivity the context to display and compute in.
     */
    public static void makePauseRouteDialog(final NavDrawerActivity navDrawerActivity) {
        final AlertDialog.Builder alertDialogBuilder = makeDialog(navDrawerActivity, R.string.pause_route);
        alertDialogBuilder.setPositiveButton(R.string.dialog_pause, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                navDrawerActivity.finish();
            }
        });

        //display smsDialog
        AlertDialog pauseRouteDialog = alertDialogBuilder.create();
        pauseRouteDialog.show();

        //customize title and button colors (to fit our theme)
        setColors(navDrawerActivity, pauseRouteDialog);
    }


    public static void makeTimeslotWarning(final Activity activity) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setMessage(activity.getResources().getString(R.string.dialog_timeslot_warning))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        //display smsDialog
        AlertDialog timeslotDialog = alertDialogBuilder.create();
        timeslotDialog.show();

        //customize title and button colors (to fit our theme)
        setColors(activity, timeslotDialog);

    }

    /**
     * Dialog for delivering a package with or without signature.
     *
     * @param context context to display in. has to be deliveryactivity.
     */
    public static void makeDeliveryDialog(final Context context) {
        final AlertDialog.Builder alertDialogBuilder = makeDialog(context, R.string.dialog_title_delivery);
        alertDialogBuilder.setPositiveButton(R.string.finish, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((DeliveryActivity) context).continueDelivery();
            }
        });
//                .setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                ((Activity)context).findViewById(R.id.continueButton).setClickable(true);
//            }
//        });

        //display smsDialog
        AlertDialog deliveryDialog = alertDialogBuilder.create();
        deliveryDialog.show();

        //customize title and button colors (to fit our theme)
        setColors(context, deliveryDialog);
    }

    /**
     * creates a dialog that prompts the user about sending sms.
     *
     * @param context context to display in
     * @param task    the current task with customer
     */
    public static void makeSMSDialog(final Context context, final Task task) {
        final AlertDialog.Builder alertDialogBuilder = makeDialog(context, R.string.dialog_sms_title);
        alertDialogBuilder.setPositiveButton(R.string.dialog_sms_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CommunicationUtil.sendSMS(context, task);
            }
        });
        //display smsDialog
        AlertDialog smsDialog = alertDialogBuilder.create();
        smsDialog.show();

        //customize title and button colors (to fit our theme)
        setColors(context, smsDialog);
    }

    /**
     * Creates a dialog for calling a customer.
     *
     * @param context context to display in
     * @param number  number to call as string
     */
    public static void makePhoneDialog(final Context context, final int title, int positive, final String number) {
        final AlertDialog.Builder alertDialogBuilder = makeDialog(context, title);
        alertDialogBuilder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                CommunicationUtil.callNumber(number, context);
            }
        });
        //display phoneDialog
        AlertDialog phoneDialog = alertDialogBuilder.create();
        phoneDialog.show();

        //customize title and button colors (to fit our theme)
        setColors(context, phoneDialog);
    }


    /**
     * Method for setting title and button colors of a dialog
     *
     * @param context
     * @param dialog
     */
    private static void setColors(Context context, AlertDialog dialog) {
        final int alertTitle = context.getResources().getIdentifier("alertTitle", "id", "android");
        TextView title_tv = (TextView) dialog.findViewById(alertTitle);
        title_tv.setTextColor(context.getResources().getColor(R.color.primary));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);

    }

    /**
     * "Hold up! HEY!" Method for playing the rapalicious toast-song.
     *
     * @param context context to play audio in.
     */
    public static void holdUp(Context context) {
        MediaPlayer mp = MediaPlayer.create(context, R.raw.holdup);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mp.start();
    }

    /**
     * Method for showing the ad-hoc popup.
     *
     * @param context   the activity where the view is shown.
     * @param adhocTask the current adhoc task to be shown.
     */
    public static void showAdHoc(Activity context, Task adhocTask) {
        View adhoc = context.findViewById(R.id.layout_adhoc);
        fillAdHocDialog(adhoc, adhocTask);
        AnimationUtil.slideView(adhoc, context);
    }

    /**
     * Hides the ad-hoc popup.
     *
     * @param context the activity where the view is.
     */
    public static void hideAdHoc(Activity context) {
        AnimationUtil.slideOutView(context.findViewById(R.id.layout_adhoc), context);
    }

    /**
     * Fill out the adhoc dialoginfo with info from the new task.
     *
     * @param adhoc the adhoc view, where all the textfields are
     * @param task  the new adhoc task
     */
    public static void fillAdHocDialog(View adhoc, Task task) {

        //Set up alert message
        TextView taskAlert = (TextView) adhoc.findViewById(R.id.adhoc_message);
        taskAlert.setText(task.getAddress());

        //Set up detailed info
        TextView taskText = (TextView) adhoc.findViewById(R.id.taskText);

        TextView taskAddress = (TextView) adhoc.findViewById(R.id.taskAddress);
        TextView taskZip = (TextView) adhoc.findViewById(R.id.taskZip);
        TextView taskSender = (TextView) adhoc.findViewById(R.id.taskSender);

        TextView taskPackages = (TextView) adhoc.findViewById(R.id.taskPackages);
        TextView taskTime = (TextView) adhoc.findViewById(R.id.taskTimeFrame);
        TextView taskExtra = (TextView) adhoc.findViewById(R.id.taskExtra);


        taskText.setText(task.getName());

        taskAddress.setText(task.getAddress());
        taskZip.setText(task.getZip() + " " + task.getCity());
        taskSender.setText(task.getSender().getName());

        taskPackages.setText(String.valueOf(task.getPackages().size()));
        taskExtra.setText(taskExtra.getText());

        if (task.getTimeSlotEnd() > 0)
            taskTime.setText(task.getTimeSlotStartString() + " - " + task.getTimeSlotEndString());

    }
}
