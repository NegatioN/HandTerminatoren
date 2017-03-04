package no.mesan.handterminator.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.MeasureSpec;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import no.mesan.handterminator.R;
import no.mesan.handterminator.RouteActivity;
import no.mesan.handterminator.util.maplogic.TaskListMapLogic;
import no.mesan.handterminator.view.SignatureView;

/**
 * @author Martin Hagen
 *
 * Fragment for "no-signature" choice when delivering tasks
 */

@EFragment(R.layout.fragment_delivery_no_signature)
public class DeliveryNoSignatureFragment extends Fragment {

    @ViewById(R.id.no_signature_message_field)
    EditText commentField;

    @AfterViews
    void setUpFragment() {
        // Listens for enter-key click and hides keyboard
        commentField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(commentField.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });
    }

    public boolean isUsed()
    {
        return (!commentField.getText().toString().equals(""));
    }

    public void saveDelivery()
    {
        String comment = commentField.getText().toString();

        //save
        Toast.makeText(getActivity(), "Levering lagret med kommentar: " + comment, Toast.LENGTH_LONG).show();

    }

}
