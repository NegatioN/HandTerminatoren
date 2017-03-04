package no.mesan.handterminator.fragment;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import no.mesan.handterminator.DeliveryActivity;
import no.mesan.handterminator.ScanningActivity;
import no.mesan.handterminator.view.CustomBarScannerView;

/**
 * @author Sondre Sparby Boge
 *
 * This fragment contains both the camera-view and the scanning-functionality.
 * Handles scanned codes and returns the result to the appropriate activity.
 */

public class CameraFragment extends Fragment implements CustomBarScannerView.ResultHandler {//, FormatSelectorDialogFragment.FormatSelectorDialogListener, MessageDialogFragment.MessageDialogListener
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";

    private static final int SCANNING_ACTIVITY = 0, DELIVERY_ACTIVITY = 1;
    private int activeActivity;

    // Camera-View
    private CustomBarScannerView scannerView;
    // Camera-variables
    private boolean flash;
    private boolean autoFocus;
    private ArrayList<Integer> selectedIndices;

    private boolean isStopped = true;
    private boolean firstTime = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        scannerView = new CustomBarScannerView(getActivity());

        // sets default settings if non preffered is set
        if (state != null) {
            flash = state.getBoolean(FLASH_STATE, false);
            autoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            selectedIndices = state.getIntegerArrayList(SELECTED_FORMATS);
        } else {
            flash = false;
            autoFocus = true;
            selectedIndices = null;
        }

        setupFormats();

        /**
         * this fragment can be called from 2 activities, this tells where
         * used for accessing the correct methods from inside this class
         */
        if( getActivity() instanceof ScanningActivity)
            activeActivity = SCANNING_ACTIVITY;
        else // instanceof DeliveryActivity
            activeActivity = DELIVERY_ACTIVITY;

        return scannerView;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setHasOptionsMenu(true);
    }

    // Called once on startup and always when the activity resumes
    @Override
    public void onResume() {
        super.onResume();

        // Handle if the activity is either created or resumed

        if(firstTime) {
            stopCamera();
            firstTime = false;
        } else if (!isStopped())
            timeout();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera(); // Stop camera to avoid overload
    }

    // Deactivates camera
    public void stopCamera() {
        scannerView.stopCamera();
        isStopped = true;
    }

    // Start or restart camera
    public void startCamera() {
        scannerView.setResultHandler(this);
        scannerView.setupLayout();
        scannerView.setLastScan();
        scannerView.startCamera();
        scannerView.setFlash(flash);
        scannerView.setAutoFocus(autoFocus);
        isStopped = false;
        Log.d("CAMERA_FRAGMENT", "Camera start()");
    }

    public boolean isStopped() {
        return isStopped;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, flash);
        outState.putBoolean(AUTO_FOCUS_STATE, autoFocus);
        outState.putIntegerArrayList(SELECTED_FORMATS, selectedIndices);
    }

    /**
     * Called when a barcode has been successfully scanned
     * and sends the result to the active Activity
     * @param rawResult is the result of the scan
     */
    @Override
    public void handleResult(Result rawResult) {
        if(rawResult == null) return;
        String in = rawResult.getContents().toString();

        // Play confirmation sound
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
            r.play();
        } catch (Exception e) { }

        // Send response to correct activity
        if(activeActivity == SCANNING_ACTIVITY)
            ((ScanningActivity)getActivity()).updateScanned(in);
        else // if activeActivity == DELIVERY_ACTIVITY
            ((DeliveryActivity)getActivity()).updateScanned(in);
    }

    /**
     * When the camera has not scanned anything in a while,
     * 'long timeout' is in CustomBarScannerView
     */
    @Override
    public void timeout() {
        Toast.makeText(getActivity(), "Timeout!", Toast.LENGTH_SHORT).show();
        Log.d("CAMERA_FRAGMENT", "TimeOut()");

        if(activeActivity == SCANNING_ACTIVITY)
            ((ScanningActivity)getActivity()).timedOut();
        else // if activeActivity == DELIVERY_ACTIVITY
            ((DeliveryActivity)getActivity()).timedOut(false);
    }

    // Barcode-formats to be scanned, comment-out any code to exclude
    public void setupFormats() {
        ArrayList<BarcodeFormat> barcodeFormats = new ArrayList<>();

        barcodeFormats.add(BarcodeFormat.CODABAR);
        barcodeFormats.add(BarcodeFormat.CODE128);
        barcodeFormats.add(BarcodeFormat.CODE39);
        barcodeFormats.add(BarcodeFormat.CODE93);
        barcodeFormats.add(BarcodeFormat.DATABAR);
        barcodeFormats.add(BarcodeFormat.DATABAR_EXP);
        barcodeFormats.add(BarcodeFormat.EAN13);
        barcodeFormats.add(BarcodeFormat.EAN8);
        barcodeFormats.add(BarcodeFormat.I25);
        barcodeFormats.add(BarcodeFormat.ISBN10);
        barcodeFormats.add(BarcodeFormat.ISBN13);
        //barcodeFormats.add(BarcodeFormat.NONE);
        //barcodeFormats.add(BarcodeFormat.PARTIAL);
        barcodeFormats.add(BarcodeFormat.QRCODE);
        barcodeFormats.add(BarcodeFormat.UPCA);
        barcodeFormats.add(BarcodeFormat.UPCE);

        if(scannerView != null)
            scannerView.setFormats(barcodeFormats);
    }
}