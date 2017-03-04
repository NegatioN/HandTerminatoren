package no.mesan.handterminator.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import no.mesan.handterminator.R;
import no.mesan.handterminator.fragment.CameraFragment;

/**
 * @author Joakim Rishaug, Martin Hagen, Lars-Erik Kasin
 */
public class AnimationUtil {

    private static final int EXPLODE_DURATION = 1000;
    public static final int EXPAND_DURATION = 200;
    private static final int ROTATION = 360;

    public static final int SCANNING_FRAGMENT = 0;

    /**
     * Slides invisible view from y-100 to y
     * when animation starts, sets view to visible
     * @param view
     * @param context
     * @return
     */
    public static long slideView(View view, Context context)
    {
        Animation slide = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        slide.setInterpolator(new AccelerateDecelerateInterpolator());

        view.setVisibility(View.VISIBLE);
        view.startAnimation(slide);

        return slide.getDuration();
    }

    /**
     * Slides visible view from y to y-100
     * when animation is finished, sets view to invisible
     * @param view
     * @param context
     * @return
     */
    public static void slideOutView(View view, Context context)
    {
        Animation slide = AnimationUtils.loadAnimation(context, R.anim.abc_slide_out_bottom);
        slide.setInterpolator(new AccelerateInterpolator());
        slide.setDuration(700);

        view.setVisibility(View.INVISIBLE);
        view.startAnimation(slide);
    }

    /**
     * Do a crossfade between two views. fromView is faded out, and toView is faded in.
     * Setting one of the params to null, let's the other view fade out or in from/to bgColor
     * @param fromView View to hide.
     * @param toView View to show.
     */
    public static void crossFade(final View fromView, View toView){
        if (fromView != null)
        fromView.animate().alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                fromView.setVisibility(View.GONE);
            }
        }).start();

        if (toView != null) {
            toView.setAlpha(0);
            toView.setVisibility(View.VISIBLE);
            toView.animate().alpha(1).start();
        }
    }

    /**
     * A standard Explosion-transition that excludes the app-bar and status-bar.
     * @return Explosion-transiton that excludes statusbar and app-bar
     */
    public static Transition getExplodeTransition()
    {
        Transition explode = new Fade();
        explode.setDuration(EXPLODE_DURATION);
        explode.excludeTarget(R.id.app_bar, true);
        explode.excludeTarget(android.R.id.statusBarBackground, true);

        return explode;
    }

    /**
     * @param gravity The gravitiy of the Slide
     * @param target the view to animate
     * @return Slide-transition with AccelerateDecelerate Interpolator
     */
    public static Transition getSlide(int gravity, View target){
        Slide s = new Slide(gravity);
        s.addTarget(target);
        s.setInterpolator(new LinearInterpolator());
        return s;
    }

    /**
     * Animates alpha to 1 or 0 on the view.
     * @param view view to animate
     * @param duration duration of fade-animation
     * @param endAction at the end of fade, execute this runnable
     */
    public static void fadeView(View view, long duration, Runnable endAction){
        view.animate().alpha(view.getAlpha() < 1 ? 1 : 0).setDuration(duration).withEndAction(endAction).start();
    }

    /**
     * Rotate-Animate the action button and change it's icon to indicate that it has a new function
     * @param state Boolean state for list state. (is being edited, or normal flow)
     * @param actionButton the actionbutton to animate
     */
    public static void buttonDeliverAnimation(final boolean state, final ImageButton actionButton)
    {
        //Animate the action button and change it's icon to indicate that it has a new function
        actionButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                actionButton.setImageResource(state ? R.drawable.ic_verify_list : R.drawable.ic_check);
            }
        }, actionButton.animate().rotationBy(state ? -ROTATION : ROTATION).getDuration());
    }

    public static ValueAnimator taskExpandAnimator(ValueAnimator a, View v)
    {
        final View view = v;
        a.setDuration(EXPAND_DURATION);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                view.getLayoutParams().height = value;
                view.requestLayout();
            }
        });
        return a;
    }

    public static void setStartPosCorner(final ImageButton floatingActionButton, final Context c, final View ripple)
    {
       /*final RelativeLayout layout = (RelativeLayout)((ScanningActivity) c).findViewById(R.id.toLayout);
       // final View layout = cameraFrame;

        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = layout.getWidth();
                int height = layout.getHeight();
                Log.d("Ripple: ", "(" + width + "," + height + ")");
                moveButtonToCorner(height,width,floatingActionButton,c);
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

            }
        });*/

        ViewPropertyAnimator animator = floatingActionButton.animate().x(1559).y(1190);
        // pos for emulator = x(1559).y(1094)| pos for tablet = x(1559).y(1190)

        animator.start();

    }

    //returnerer feil verdier, blir plassert utenfor view
    public static void moveButtonToCorner(int h, int w, ImageButton floatingActionButton, Context c)
    {
        final int buttonMargin = (int) ((Activity) c).getResources().getDimension(R.dimen.fab_margin);

        int offset = (int) (buttonMargin * Resources.getSystem().getDisplayMetrics().density);

        ViewPropertyAnimator animator = floatingActionButton.animate().x(w - offset).y(h - offset);

        animator.start();
    }

    /**
     * Animates our FloatingActionButton and camera-view for idle- and start-camera animations.
     * @param floatingActionButton Our floatingActionButton that moves to center of parent-frame
     * @param ripple The main view you want to animate in and out
     * @param exitButton sets an button visible after animation - optional
     */
    public static void animateButton(final ImageButton floatingActionButton, final View ripple,
                                     Context c, final CameraFragment fragment, final FrameLayout layout,
                                     int radius, double percentScanned, final ImageButton exitButton) {

        final int buttonMargin = (int) ((Activity) c).getResources().getDimension(R.dimen.add_button_margin);
        ripple.setVisibility(View.VISIBLE);

        //set button invisible, and move it to the corner
        ViewPropertyAnimator animator = moveButtonToCorner(floatingActionButton,ripple,radius, buttonMargin, false);

        //at the same time as button-animation. circularReaveal the camera
        animator.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                circularRevealCamera(floatingActionButton, ripple, fragment, layout, exitButton);
            }
        });

        //get correct drawable for button depending on scanned packages.
        floatingActionButton.setImageResource(ImageUtil.getButtonDrawable(percentScanned));

        //set button visible if one or more scanned.
        if(percentScanned > 0)
            floatingActionButton.setVisibility(View.VISIBLE);

    }

    /**
     *
     * @param floatingActionButton button to animate
     * @param ripple view to get corner of
     * @param radius radius of button
     * @param buttonMargin margin for button
     * @return Animator for moving the FloatingActionButton to lower right corner and setting it invisible.
     */
    public static ViewPropertyAnimator moveButtonToCorner(ImageButton floatingActionButton, View ripple, float radius, int buttonMargin, boolean visible){
        if(!visible)
            floatingActionButton.setVisibility(View.INVISIBLE);

        float xPosCorner = (ripple.getRight() - ((radius * 2) + (buttonMargin)));
        float yPosCorner = (ripple.getBottom() - ((radius * 2) + (buttonMargin)));

        return floatingActionButton.animate().x(xPosCorner).y(yPosCorner);
    }

    /**
     *
     * @param floatingActionButton button we want to animate to middle
     * @param ripple the view we want to animate to the middle of
     * @param move determines if button is to move, needed for further animations
     * @return animator for the button passed in
     */
    public static ViewPropertyAnimator moveButtonToCenter(ImageButton floatingActionButton, View ripple, float radius, boolean move){

        if(!move)
            return floatingActionButton.animate().x(floatingActionButton.getX());

        float xPosCenter = ((ripple.getLeft() + ripple.getRight())/2)-radius;
        float yPosCenter = ((ripple.getTop() + ripple.getBottom())/2)-radius;

        return floatingActionButton.animate().x(xPosCenter).y(yPosCenter);
    }

    /**
     * The method is called when our camera should time out. moves button to center
     * and ripples the camera in.
     */
    public static void cameraTimeout(ImageButton floatingActionButton, final View ripple, final CameraFragment fragment, final FrameLayout layout, boolean moveBtn){

        final float radius = getRadius(floatingActionButton);
        final float maxRadius = Math.max(ripple.getWidth(), ripple.getHeight());

        //animates movement from corner to middle of camera
        ViewPropertyAnimator anim = moveButtonToCenter(floatingActionButton,ripple, radius, moveBtn);

        //splash-fades after move-animation
        anim.setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                ripple.animate().alpha(0);
                ripple.setVisibility(View.VISIBLE);
                ripple.animate().alpha(1);
                circularFadeCamera(radius, maxRadius, ripple, fragment, layout);
            }
        });
        if(moveBtn)
            floatingActionButton.setImageResource(R.drawable.ic_camera_alt_white_48dp);
    }

    /**
     * Creates a circular animation for the invisible view passed in, from the button passed in.
     * @param floatingActionButton  the button to press when animating
     * @param ripple the ripple we want to animate over the camera
     * @param exitButton sets an button visible after animation - optional
     */
    public static void circularRevealCamera(ImageButton floatingActionButton, final View ripple,
           CameraFragment fragment, final FrameLayout layout, final ImageButton exitButton){

        //get the center for the start of the circular animation
        int radius = getRadius(floatingActionButton);
        int x = (int)floatingActionButton.getX() + radius;
        int y = (int) floatingActionButton.getY() + radius;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(ripple.getWidth(), ripple.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim =
                    ViewAnimationUtils.createCircularReveal(ripple, x, y, 0, finalRadius);

        anim.setDuration(1000);

        fragment.startCamera();

        //make the view visible and start the animation
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("CAMERA_FRAGMENT", "AnimationUtil Camera start");
                layout.setVisibility(View.VISIBLE);
                ripple.animate().alpha(0);
                if(exitButton != null)
                    exitButton.setVisibility(View.VISIBLE);
            }

        });
        anim.start();
    }


    /**
     * Does a circular animation from maxRadius down to minimum radius on the view
     * It also stops the camera and sets the ripple-view to invisible after animation.
     * @param radius minimum radius
     * @param maxRadius max radius
     * @param ripple view that ripples circularly
     * @param fragment camerafragment to stop
     * @param layout parent layout to use for positioning.
     */
    public static final void circularFadeCamera(float radius, float maxRadius, final View ripple,
                                                    final CameraFragment fragment, final FrameLayout layout){

        float xPosCenter = ((ripple.getLeft() + ripple.getRight())/2);
        float yPosCenter = ((ripple.getTop() + ripple.getBottom())/2);

        Animator anim = ViewAnimationUtils.createCircularReveal(ripple, (int)xPosCenter, (int)yPosCenter, maxRadius, radius);

        anim.setDuration(1000);

        fragment.stopCamera();
        layout.setVisibility(View.INVISIBLE);

        // make the view visible and start the animation
        if(ripple.getVisibility() == View.VISIBLE) {
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ripple.setVisibility(View.INVISIBLE);
                }
            });
        }
        anim.start();
    }

    /**
     * Returns the radius of a view.
     * @param floatingActionButton view to get radius of
     * @return radius as int
     */
    public static int getRadius(ImageButton floatingActionButton)
    {
        return (floatingActionButton.getRight() - floatingActionButton.getLeft()) / 2;
    }

    /**
     * is the actionbutton placed in the center of the first view?
     * @param ripple first view
     * @param actionButton button to check positon of
     * @return true/false button in middle of view
     */
    public static boolean inMiddle(View ripple, ImageButton actionButton)
    {
        float radius = getRadius(actionButton);
         float middle = getMiddle(ripple, radius);
         return (middle == actionButton.getX());
    }

    public static float m(View ripple)
    {
        return ripple.getRight() + ripple.getLeft();
    }

    /**
     * @param view view
     * @param radius radius of view
     * @return position of center for the view
     */
    public static float getMiddle(View view, float radius)
    {
        return ((view.getLeft() + view.getRight()) / 2) - radius;
    }

////////////// DEPRECATED METHODS
    @Deprecated
    public static void drawViewOnView(Resources resources, View background, View destination){
        Bitmap bitmap = AnimationUtil.getBitmapFromView(background);
        Drawable bgDrawable = new BitmapDrawable(resources , bitmap);
        destination.setBackground(bgDrawable);
    }
    /**
     * Returns a bitmap showing a screenshot of the view passed in.
     */
    @Deprecated
    public static Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }
}
