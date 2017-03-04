package no.mesan.handterminator.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.DeliveryActivity;
import no.mesan.handterminator.R;
import no.mesan.handterminator.ScanningActivity;
import no.mesan.handterminator.adapter.ScanListAdapter;
import no.mesan.handterminator.model.db.*;
import no.mesan.handterminator.model.db.Package;
import no.mesan.handterminator.view.CustomAutoCompleteTextView;

/**
 * @author Sondre Sparby Boge, Martin Hagen
 *
 * Contains methods frequently used in scanning.
 * Such as the functions for manual input of kolli.
 *
 * These methods are static since there are
 * multiple scanning-activities.
 *
 * This class is used in Scanning- and DeliveryActivity
 */
public class ScanningUtil {
    private static final int SNACKBAR_DURTION = 8000; // 8 sec

    /**
     * Opens an autoCompleteDialog that allows the user to click an option
     * without writing the whole kolli. If a kolli is selected the dialog
     * disappears, and if a correct kolli is inputted and the 'enter'-key is pressed.
     * After a valid kolli is inputed a SnackBar appears with an 'undo' button to undo the scan.
     *
     * @param context, the scanning-activity itself
     * @param scanList, list of all kollis
     */
    public static void manualKolliDialog(final Context context, List<Task> scanList) {
        //content of dropdown list - depends on activity
        String[] kollis = (context instanceof ScanningActivity)
                ? getKollisTask(scanList) : getKollisPackages(scanList.get(0).getPackages());

        // Adapter is basically the dropdown itself
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.light_dropdown_item_1line, kollis);

        //Generates the autocomplete list/view
        final CustomAutoCompleteTextView autoCompleteTextView = makeAutoCompleteTV(context,adapter);

        // Builds the dialog that shows the AutoComplete-view
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.manual_kolli_input);
        builder.setView(autoCompleteTextView);

        //Adds onEnterClickedListener to native keyboard, for completing "scanning"
        builder = addEnterListener(context, builder, autoCompleteTextView);

        // Builds the dialog so we can dismiss it in OnItemClickListener
        final AlertDialog dialog = builder.show();

        //adds onClickListener to all list items
        addOnClickListener(context, autoCompleteTextView, dialog);

        // Adds padding from parent
        ViewGroup parent = (ViewGroup) autoCompleteTextView.getParent();
        parent.setPadding(30, 30, 30, 30);
    }

    /**
     * Defines relevant info for the AutoComplete-View
     * @param context
     * @param adapter
     * @return a new CustomAutoCompleteTextView
     */
    private static CustomAutoCompleteTextView makeAutoCompleteTV(Context context,ArrayAdapter<String> adapter)
    {
        final CustomAutoCompleteTextView textView = new CustomAutoCompleteTextView(context);
        textView.setAdapter(adapter);
        textView.setTextColor(Color.DKGRAY);
        textView.setThreshold(2);   // how many chars are required before completion-hints
        //textView.setDropDownHeight(380); // how many visable elements at a time in dropdown, defined in pixels
        textView.setSingleLine();
        textView.setRawInputType(Configuration.KEYBOARD_12KEY); // keyboard-type - KEYBOARD_12KEY for num

        return textView;
    }

    /**
     * Adds enterKeyListener to native keyboard
     * Used after complete kolli is typed
     * @param context
     * @param builder
     * @param autoCompleteTextView
     * @return same builder sent in, only with listener
     */
    private static AlertDialog.Builder addEnterListener(final Context context, AlertDialog.Builder builder, final CustomAutoCompleteTextView autoCompleteTextView)
    {
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String input = autoCompleteTextView.getText().toString();
                    try {
                        if(context instanceof ScanningActivity)
                            ((ScanningActivity) context).updateScanned(input);
                        else if(context instanceof DeliveryActivity)
                            ((DeliveryActivity) context).updateScanned(input);
                    } catch (Exception e) {
                        Toast.makeText(context, "Feil i input. Prøv igjen", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        return builder;
    }

    /**
     * Handles when an item in the dropdown is clicked.
     * When an item in the dropdown is clicked the correct adapter updates its list
     * @param context
     * @param autoCompleteTextView - dropdown list
     * @param dialog - reference to its dialog, can't close unless the AlertDialog is initialized
     */
    private static void addOnClickListener(final Context context, final CustomAutoCompleteTextView autoCompleteTextView, final Dialog dialog)
    {
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String input = (String) parent.getItemAtPosition(position);
                try {
                    if(context instanceof ScanningActivity)
                        ((ScanningActivity) context).updateScanned(input);
                    else if(context instanceof DeliveryActivity)
                        ((DeliveryActivity) context).updateScanned(input);
                } catch (Exception e) {
                    Toast.makeText(context, "Feil i input. Prøv igjen", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }

    /**
     * Gets and returns any active kollis - used for list in dropdown
     * Used if this class is called from ScanningActivity
     * @param taskList list of tasks - used to find unscanned packages
     * @return
     */
    private static String[] getKollisTask(List<Task> taskList) {
        List<Package> packages = new ArrayList<>();

        // Gets all unscanned kollis
        for(Task t : taskList) for(Package p : t.getPackages())
            if(!(p.isScannedIn())) packages.add(p);

        String[] out = new String[packages.size()];

        for(int i = 0; i < out.length; i++)
            out[i] = packages.get(i).getKolli()+"";

        return out;
    }

    /**
     * Gets and returns any active kollis - used for list in dropdown
     * Used if this class is called from DeliveryActivity
     * @param packageList list of unscanned packages
     * @return
     */
    private static String[] getKollisPackages(List<Package> packageList) {
        List<Package> packages = new ArrayList<>();

        Log.d("Scanner en task","True");

        // Gets all unscanned kollis
        for(Package p : packageList){
            if(!(p.isScannedOut())) {
                packages.add(p);
                Log.d("Ikke scanned pakke", p.getKolli());
            }

        }

        String[] out = new String[packages.size()];

        for(int i = 0; i < out.length; i++)
            out[i] = packages.get(i).getKolli()+"";

        return out;
    }

    /**
     * Shows snackbar with an 'undo'-button with the option to undo the last scanned kolli
     *
     * @param context - identify which class used this method
     * @param kolli - identifier of last scanned package
     */
    public static void showScannedSnackBar(final Context context,final String kolli) {
        SnackbarManager.show(
                Snackbar.with(context) // context
                        .text("Scannet: " + kolli) // text to display
                        .actionLabel("Angre") // action button label
                        .duration(SNACKBAR_DURTION)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                unscanKolli(context,kolli);
                            }
                        }) // action button's ActionClickListener
                , (android.app.Activity) context);
    }

    // Sets the package with the corresponding kolli to not-scanned
    private static void unscanKolli(Context context, String kolli) {
        Log.d("Unscanning", kolli);
        if(context instanceof ScanningActivity)
            ((ScanningActivity) context).unscannPackage(kolli);
        else
            ((DeliveryActivity) context).unscannPackage(kolli);
    }
}
