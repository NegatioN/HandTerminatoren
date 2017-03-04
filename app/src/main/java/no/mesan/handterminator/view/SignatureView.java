package no.mesan.handterminator.view;

import android.content.Context;

import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.gcacace.signaturepad.views.SignaturePad;

/**
 * @author Sondre Sparby Boge
 *
 * This view extends the SignaturePad-library by gcacace.
 * The SignaturePad is based on bezier-curves to make smooth
 * signatures on a canvas. However since we need to be able
 * to tell whether the view has been signed or not, and also
 * get the canvas of the signature itself we need to implement
 * these methods ourselves.
 *
 */
public class SignatureView extends SignaturePad {

    // If the canvas have been touched
    private boolean isSigned = false;

    // Signature bounds, init-values exceeds the view-size
    private Rect bounds = new Rect(3000, 0, 0, 3000); // (left, top, right, bottom)
    private int padding; // Padding to extend the signature-bounds when saving the image

    private int signLength = 0; // Length of the signature
    private final int signLengthTolerance = 50; // Minimum length of the signature

    private float lastX = -1;
    private float lastY = -1;

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMinWidth(7);              // default 3
        setMaxWidth(12);              // default 7
        //setVelocityFilterWeight((float)0.9);  // default 0.9
        //setPenColor();
    }

    // Clears canvas and signature
    @Override
    public void clear() {
        super.clear();
        isSigned = false;
        signLength = 0;
        lastX = -1;
        lastY = -1;
        bounds = new Rect(3000, 0, 0, 3000);
    }

    // Returns true if the signature is long enough
    public boolean isSigned() {
        return isSigned && signLength > signLengthTolerance;
    }

    //Called for every touch, move or release of the signature-view
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean s = super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        if(lastX == -1 || lastY == -1) {
            lastX = x;
            lastY = y;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            signLength += Math.sqrt((int) Math.abs(x - lastX) ^ 2 + (int) Math.abs(y - lastY) ^ 2);
            setBounds(x, y);
            isSigned = true;
        }

        lastX = x;
        lastY = y;

        return s;
    }

    // Checks and sets new bounds if they exceed previous bounds
    private void setBounds(float x, float y) {
        if(x < bounds.left && x >= 0) bounds.left = (int) x;
        if(x > bounds.right && x <= getWidth()) bounds.right = (int) x;
        if(y < bounds.bottom && y >= 0) bounds.bottom = (int) y;
        if(y > bounds.top && y <= getHeight()) bounds.top = (int) y;
    }

    // Returns a new Rect-object containing the bounds with padding
    public Rect getRect() {
        padding = ( (bounds.right - bounds.left) + (bounds.top - bounds.bottom) ) / (2 * 10) ;  // padding = 10% of width+height/2
        if(padding < 20) padding = 20;                                                          // padding at least 20px

        int l = bounds.left - padding;
        int t = bounds.top + padding;
        int r = bounds.right + padding;
        int b = bounds.bottom - padding;

        if(l < 0) l = 0;
        if(t > getHeight()) t = getHeight();
        if(r > getWidth()) r = getWidth();
        if(b < 0) b = 0;

        Rect out = new Rect(l, t, r, b);
        return out;
    }

    public int getPadding() {
        return padding;
    }
}






    /******* OLD VERSION, use this if SignaturePad is too slow on tablet *******/

    /*
    private Bitmap bitmap;          // Bitmap-image
    private static Canvas canvas;   // Canvas containing Bitmap-image
    private Path path;              // Currently drawn path
    private Paint bitmapPaint;      // Paint drawn on the bitmap
    private Paint paint;            // Paint drawn on the Canvas

    // If the canvas has been touched
    private boolean signed = false;

    public SignatureView(Context context) {
        super(context);
    }

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(6);

        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    public SignatureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // initiates the bitmap and sets the width & height
    public void initBitmap() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    // When something touches the canvas
    @Override
    protected void onDraw(Canvas canvas) {
        // Background-color on the canvas
        canvas.drawColor(getResources().getColor(R.color.superWhite));
        // Redraws to the bitmap
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);

        // Draws the path to the canvas
        canvas.drawPath(path, paint);
    }

    // Validates signature if length is above 150px long
    public boolean isSigned() {
        return signed && signLength > 150;
    }
*/

    /**** Specific methods for touch, release and moving on the view ****/
/*
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    // Signature bounds
    private Rect bounds = new Rect(3000, 0, 0, 3000); // (left, top, right, bottom)

    // Length of the signature, used for verification
    private int signLength = 0;
    private int padding = 20;
*/
    /**
     * When view is pressed.
     * Starts new path.
     */
  /*  private void touch_start(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }
*/
    /**
     * When moving but is still touching the view.
     * Updates the path.
     */
  /*  private void touch_move(float x, float y) {//, float eventStroke) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            signLength += Math.sqrt((int)dx^2 + (int)dy^2);
        }
    }
*/
    /**
     * When touch is released
     * Saves then resets path
     */
  /*  private void touch_up() {
        path.lineTo(mX, mY);            // saves the drawn path to the canvas
        canvas.drawPath(path, paint);   // clear path to avoid duplicate draws
        path.reset();
        signed = true;
    }

    // Checks and sets new bounds if they exceed previous bounds
    private void setBounds(float x, float y) {
        if(x < bounds.left && x >= padding) bounds.left = (int) x;
        if(x > bounds.right && x <= getWidth() - padding) bounds.right = (int) x;
        if(y < bounds.bottom && y >= padding) bounds.bottom = (int) y;
        if(y > bounds.top && y <= getHeight() - padding) bounds.top = (int) y;
    }

    // Handles different touches
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        int pointerId = event.getPointerId(0);

        setBounds(x, y);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                touch_start(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:

                touch_move(x, y);//, eventStroke);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:

                touch_up();
                invalidate();
                break;
        }

        return true;
    }

    public void clearCanvas() {
        initBitmap();

        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));

        signLength = 0;
        signed = false;
    }

    // Returns the Rect-object containing the bounds
    public Rect getRect() {
        return bounds;
    }

    public int getPadding() {
        return padding;
    }
}*/
