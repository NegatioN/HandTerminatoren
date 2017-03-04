package no.mesan.handterminator.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.R;
import no.mesan.handterminator.adapter.DrawerAdapter;
import no.mesan.handterminator.model.Drawer;

/**
 *@author Joakim Rishaug
 * The class takes in a context, toolbar, drawerlayout and recyclerview
 * And configures all these components from xml to work together as a navigation-drawer.
 */
public class NavDrawerUtil {


    /**
     * Defines what happens when drawer-state changes. onOpen, onClosed, onSlide
     * also sets the drawerToggle itself
     * @param context The actionbaractivity to run on
     * @param toolbar Toolbar that's appended to activity
     * @param mDrawerLayout DrawerLayout for drawer
     * @param recyclerView The recyclerview to use as a list in the navdrawer.
     */
    public static void setupNavDrawer(final ActionBarActivity context, Toolbar toolbar, DrawerLayout mDrawerLayout, RecyclerView recyclerView){
        ActionBarDrawerToggle toggle = setupDrawerToggle(context,toolbar,mDrawerLayout);
        setupDrawerAdapter(context,recyclerView, mDrawerLayout);
        setupSyncState(mDrawerLayout, toggle);


        //Create rounded profile picture
        ImageView profilePic = (ImageView)context.findViewById(R.id.iv_profile);
        Bitmap img = ((BitmapDrawable)profilePic.getDrawable()).getBitmap();
        profilePic.setImageBitmap(getRoundedImage(img));
    }

    /**
     * Sets up our drawer-toggle. Interacts with the toolbar
     * @param context our ActionbarActivity
     * @param toolbar The toolbar of the activity
     * @param mDrawerLayout Drawerlayout
     * @return
     */
    private static ActionBarDrawerToggle setupDrawerToggle(final ActionBarActivity context, Toolbar toolbar, final DrawerLayout mDrawerLayout){
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(context, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_closed) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                context.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                context.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /* @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(slideOffset < 0.6f)
                    toolbar.setAlpha(1-slideOffset);
            }
             */
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        return mDrawerToggle;
    }
    /**
     * Defines our drawerAdapter, setting layoutmanager and constructs dummy drawer.
     * @param context context
     * @param recyclerView the recyclerview to put in the drawer
     */
    private static void setupDrawerAdapter(Context context, RecyclerView recyclerView, DrawerLayout mDrawerLayout){
        DrawerAdapter adapter = new DrawerAdapter(mDrawerLayout);
        recyclerView.setHasFixedSize(true);
        adapter.setDrawers(constructDummyDrawer());

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Method to give our Hamburger the vector-drawable antimated arrow.
     * This animates in a separate thread
     * @param mDrawerLayout drawerlayout
     * @param mDrawerToggle the drawertoggle
     */
    private static void setupSyncState(DrawerLayout mDrawerLayout, final ActionBarDrawerToggle mDrawerToggle){
        //runs the thread to syncronize Hamburger with drawer-state
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    //TODO create a method that defines drawers for our given navdrawer
    //Helper method for dummyDrawer
    private static List<Drawer> constructDummyDrawer(){
        List<Drawer> drawers = new ArrayList<>();
        String[] array = {"Profil", "Nødsituasjon", "Ring kjøreleder", "Hjelp", "Logg ut"};
        for(int i = 0; i < array.length; i++){
            switch (i) {
                case 0: drawers.add(new Drawer(R.drawable.ic_profile, array[i])); break;
                //case 1: drawers.add(new Drawer(R.drawable.ic_statistics, array[i])); break;
                case 1: drawers.add(new Drawer(R.drawable.ic_emergency, array[i])); break;
                case 2: drawers.add(new Drawer(R.drawable.ic_phone, array[i])); break;
                case 3: drawers.add(new Drawer(R.drawable.ic_help, array[i])); break;
                case 4: drawers.add(new Drawer(R.drawable.ic_logout, array[i])); break;
                default: break;
            }
        }
        return drawers;
    }

    /**
     * Create a rounded image of the provided profile picture, for displaying in drawer
     * @param bmp Bitmap to be rounded
     * @return
     */
    public static Bitmap getRoundedImage(Bitmap bmp){
        Bitmap output = Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

        canvas.drawCircle(bmp.getWidth()/2, bmp.getHeight()/2, bmp.getWidth()/2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bmp, rect, rect, paint);
        return output;
    }
}
