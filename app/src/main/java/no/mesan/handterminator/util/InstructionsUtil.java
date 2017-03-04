package no.mesan.handterminator.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import no.mesan.handterminator.NavDrawerActivity;
import no.mesan.handterminator.R;
import no.mesan.handterminator.RouteListActivity;
import no.mesan.handterminator.ScanningActivity;

/**
 * @author Lars-Erik Kasin
 *         Util class for handling user support and instructions
 */
public class InstructionsUtil {

    static int step = 0;
    /**
     * Shows the tutorial overlay for the given activity. Called from the navdrawer and
     * on first launch.
     *
     * @param context activity to show tutorial for
     */
    public static void showOverlay(final Activity context) {
        final ImageView overlay = (ImageView) context.findViewById(R.id.overlay_instructions);
        int overlayId = getTutorialOverlay(context);

        overlay.setImageResource(overlayId);

        if (overlayId < 0)
            overlay.setVisibility(View.GONE);
        else {
            overlay.setVisibility(View.VISIBLE);
            overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOverlay(context);
                }
            });
        }
    }

    public static int getTutorialOverlay(Activity context) {
        if (context instanceof RouteListActivity)
            switch (step++) {
                case 1:
                    return R.drawable.tutorial_ruteinfo;
                case 2:
                    return R.drawable.tutorial_rutekart;
                case 3:
                    return R.drawable.tutorial_fab;
                case 4:
                    step = 0;
                    return -1;
                default:
                    step = 1;
                    return R.drawable.tutorial_ruteliste;
            }
        else if (context instanceof ScanningActivity)
            switch (step++) {
                case 1:
                    return R.drawable.tutorial_scanlist;
                case 2:
                    step = 0;
                    return -1;
                default:
                    step = 1;
                    return R.drawable.tutorial_scancam;
            }
        else if (context instanceof NavDrawerActivity)
            switch (step++) {
                case 1:
                    step = 0;
                    return -1;
                default:
                    step = 1;
                    return R.drawable.tutorial_worklist;
            }
        return -1;
    }


    //TODO: Connect this check to the current user, not overall system.
    //TODO: Overlays look bad with transitions. Fade in overlay may work fine.
    /**
     * Check if the given activity is visited for the first time and, if so, show it's tutorial.
     *
     * @param context activity to show tutorial for
     * @param id      Sharedpreference string for checking the particular activity
     */
    public static void checkVisited(Activity context, String id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean visited = prefs.getBoolean(id, false);
        if (!visited) {
            showOverlay(context);
            prefs.edit().putBoolean(id, true).commit();
        }
    }
}
