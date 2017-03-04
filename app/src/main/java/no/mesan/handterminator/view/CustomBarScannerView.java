package no.mesan.handterminator.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.Collection;
import java.util.List;

import me.dm7.barcodescanner.core.CameraPreview;
import me.dm7.barcodescanner.core.CameraUtils;
import me.dm7.barcodescanner.core.DisplayUtils;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;

/**
 * @author Sondre Sparby Boge
 *
 * Includes several methods from the BarcodeScannerView from ZXing.
 * Since we want custom scanning-gui we can't extend this directly.
 *
 * Any class using the BarcodeScanner must implement the ResultHandler-interface
 */
public class CustomBarScannerView extends FrameLayout implements Camera.PreviewCallback  {

    /***** Extends BarcodeScannerView *****/

    /**
     * Interface that any class using the camera must implement
     */
    public interface ResultHandler {
        /**
         * Handles result after a successful barcode-scan
         * @param rawResult is the result of the scan
         */
        public void handleResult(Result rawResult);

        /**
         * Runs when the camera times out after
         * a while (timeout)
         */
        public void timeout();
    }

    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner scanner;
    private List<BarcodeFormat> formats;
    private ResultHandler resultHandler;

    // time before new scan can be performed (in millis)
    private long scanPause = 3500;
    // time-stamp of last scan
    private long lastScan = System.currentTimeMillis() - scanPause;
    // time before camera times out and is disabled
    private long timeout = 60000;

    public CustomBarScannerView(Context context) {
        super(context);
        setupLayout();
        setupScanner();
    }

    public CustomBarScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupLayout();
        setupScanner();
    }

    /**
     * Sets acceptable barcode-formats to scan
     * @param formats, the different barcode formats
     */
    public void setFormats(List<BarcodeFormat> formats) {
        this.formats = formats;
        setupScanner();
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public Collection<BarcodeFormat> getFormats() {
        if(formats == null) {
            return BarcodeFormat.ALL_FORMATS;
        }
        return formats;
    }

    public void setLastScan() {
        lastScan = System.currentTimeMillis() - scanPause;
    }

    public void setupScanner() {
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        scanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for(BarcodeFormat format : getFormats()) {
            scanner.setConfig(format.getId(), Config.ENABLE, 1);
        }
    }

    /**
     * Called every frame the camera-preview updates
     * @param data is screen-data
     * @param camera is the camera-object itself
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        // required for continuous scanning
        camera.setPreviewCallback(this);

/*      // Used if portrait mode
        if(DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
            int tmp = width;
            width = height;
            height = tmp;
            data = rotatedData;
        }
        */

        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);

        int result = scanner.scanImage(barcode);

        // disable camera if no scan in a while (timeout)
        if(lastScan != -1 && System.currentTimeMillis() - lastScan > timeout) {
            camera.setPreviewCallback(null);
            //stopCamera();
            resultHandler.timeout();
            return;
        }

        // if successful scan, and has been a while (scanPause) since last scan
        if (result != 0 && System.currentTimeMillis() - lastScan > scanPause) {
            lastScan = System.currentTimeMillis();
            if(resultHandler != null) {
                SymbolSet syms = scanner.getResults();
                Result rawResult = new Result();
                for (Symbol sym : syms) {
                    String symData = sym.getData();
                    if (!TextUtils.isEmpty(symData)) {
                        rawResult.setContents(symData);
                        rawResult.setBarcodeFormat(BarcodeFormat.getFormatById(sym.getType()));
                        break;
                    }
                }
                resultHandler.handleResult(rawResult);
            }
        }
    }


    /***** Extends FrameLayout *****/
    private Camera mCamera;
    private CameraPreview mPreview;
    private CustomViewFinderView mViewFinderView;
    // the rectangle that contains camera-gui
    private Rect mFramingRectInPreview;

    public void setupLayout() {
        mPreview = new CameraPreview(getContext());
        mViewFinderView = new CustomViewFinderView(getContext());
        addView(mPreview);
        addView(mViewFinderView);
    }

    public void startCamera() {
        mCamera = CameraUtils.getCameraInstance();
        if(mCamera != null) {
            setupLayout();
            mViewFinderView.setupViewFinder();
            mPreview.setCamera(mCamera, this);
            mPreview.initCameraPreview();
            setLastScan();
        }
    }

    public void stopCamera() {
        if(mCamera != null) {
            mPreview.stopCameraPreview();
            mPreview.setCamera(null, null);
            mCamera.release();
            mCamera = null;
            lastScan = -1;
            removeAllViews();
        }
    }

    // sets sizes & resolutions for the camera-gui rectangle
    public synchronized Rect getFramingRectInPreview(int width, int height) {
        if (mFramingRectInPreview == null) {
            Rect framingRect = mViewFinderView.getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point screenResolution = DisplayUtils.getScreenResolution(getContext());
            Point cameraResolution = new Point(width, height);

            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }

            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;

            mFramingRectInPreview = rect;
        }
        return mFramingRectInPreview;
    }

    public void setFlash(boolean flag) {
        if(mCamera != null && CameraUtils.isFlashSupported(mCamera)) {
            Camera.Parameters parameters = mCamera.getParameters();
            if(flag) {
                if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCamera.setParameters(parameters);
        }
    }

    public boolean getFlash() {
        if(mCamera != null && CameraUtils.isFlashSupported(mCamera)) {
            Camera.Parameters parameters = mCamera.getParameters();
            if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void toggleFlash() {
        if(mCamera != null && CameraUtils.isFlashSupported(mCamera)) {
            Camera.Parameters parameters = mCamera.getParameters();
            if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            mCamera.setParameters(parameters);
        }
    }

    public void setAutoFocus(boolean state) {
        if(mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }
}