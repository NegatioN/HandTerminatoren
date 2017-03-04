package no.mesan.handterminator.fragment;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.widget.Toast;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import no.mesan.handterminator.R;
import no.mesan.handterminator.view.SignatureView;

/**
 * @author Sondre Sparby Boge
 *
 * Fragment that contains all views and functions
 * for drawing and saving signatures.
 */

@EFragment(R.layout.fragment_delivery_signature)
public class DeliverySignatureFragment extends Fragment {

    @ViewById
    SignatureView signatureView;

    // Name of the signature-image to be saved on the device
    private String fileName = "uncategorized_signature";

    /**
     * Saves the signature as a jpeg-image in the signatureView
     * to the directory '/storage/emulated/0/postnord/signatures'
     */
    //@OptionsItem(R.id.action_save_signature)
    public boolean saveSignature() {
        // Do not save if not signed
        if(!signatureView.isSigned()) return false;

        // Prepares signature-view for save
        signatureView.setDrawingCacheEnabled(true); // Locks the drawing-cache
        signatureView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        signatureView.layout(0, 0, signatureView.getWidth(), signatureView.getHeight());
        signatureView.buildDrawingCache(true);

        // Gets the borders for the signature
        Rect rect = signatureView.getRect();
        int l = rect.left;
        int b = rect.bottom;
        int r = rect.right;
        int t = rect.top;

        Bitmap bm;
        // If signature-coordinates exceeds bounds, get the whole canvas
        if (l >= 0 && l <= signatureView.getWidth() && b >= 0 && b <= signatureView.getHeight() && r >= 0 && r <= signatureView.getWidth() && t >= 0 && t <= signatureView.getHeight())
            bm = Bitmap.createBitmap(signatureView.getDrawingCache(), l, b, r - l, t - b);
        else
            bm = Bitmap.createBitmap(signatureView.getDrawingCache());

        // Releases the drawing cache for re-use
        signatureView.setDrawingCacheEnabled(false);

        if (bm != null) {
            try {
                // Sets the path for the signature directory and creates them if needed
                String path = Environment.getExternalStorageDirectory().toString() + "/postnord";
                existsOrCreateDir(path);
                path += "/signatures";
                existsOrCreateDir(path);

                File file = new File(path, fileName + ".jpg");
                OutputStream fOut = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                fOut.flush();
                fOut.close();

                //Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.delivery_confirmed), Toast.LENGTH_SHORT).show();
                Log.d("Signatur lagret", file.getAbsolutePath() + "");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // If a directory exists, if not create it
    private boolean existsOrCreateDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
            return false;
        }
        return true;
    }

    public void clearSignature() {
        signatureView.clear();
    }

    public boolean isSigned() {
        return signatureView.isSigned();
    }

    // Sets the filename for the signature-image
    public void setTargetLocation(String fileName) {
        this.fileName = fileName;
    }
}