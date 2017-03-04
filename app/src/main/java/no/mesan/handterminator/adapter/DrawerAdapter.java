package no.mesan.handterminator.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import no.mesan.handterminator.EmergencyActivity_;
import no.mesan.handterminator.MainActivity_;
import no.mesan.handterminator.R;
import no.mesan.handterminator.RouteListActivity;
import no.mesan.handterminator.UserProfileActivity_;
import no.mesan.handterminator.model.Drawer;
import no.mesan.handterminator.util.DialogUtil;
import no.mesan.handterminator.util.InstructionsUtil;

/**
 * @author Joakim Rishaug
 * Adapter-class for the drawer recyclerview. Creates the viewHolders and Views which
 * populate the navigationDrawer.
 */

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {

    List<Drawer> drawerInfo = Collections.emptyList();
    private LayoutInflater inflater;
    private DrawerLayout mDrawerLayout;
    private Context context;

    public DrawerAdapter(DrawerLayout mDrawerLayout){
        this.mDrawerLayout = mDrawerLayout;
    }

    @Override
    public DrawerAdapter.DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        this.context = context;
        inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.row_drawer, parent, false);

        v.setClickable(true);
        v.setBackground(context.getDrawable(R.drawable.ripple_nav));

        final DrawerViewHolder vh = new DrawerViewHolder(v);


        //TODO define drawer behaviour
        return vh;
    }

    /**
     * Sets the icon and title of a drawer in the navigation drawer.
     * @param holder current view(inflated) to be modified.
     * @param position position in the recyclerview
     */
    @Override
    public void onBindViewHolder(DrawerAdapter.DrawerViewHolder holder, int position) {

        Drawer currentDrawer = drawerInfo.get(position);
        holder.title.setText(currentDrawer.getTitle());
        holder.icon.setImageResource(currentDrawer.getIconId());

    }

    @Override
    public int getItemCount() {
        return drawerInfo.toArray().length;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Internal class for objects that appears in our recycler-view
     */
    class DrawerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView icon;
        private TextView title;


        public DrawerViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.drawer_icon);
            title = (TextView) itemView.findViewById(R.id.drawer_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            RecyclerView parent = (RecyclerView) v.getParent();
            Activity context = (Activity) parent.getContext();
            mDrawerLayout.closeDrawers();
            //if we're handling routelistactivity, we need to disable other transitions
            //to keep the "an image" of the old acitivty below
            if(context instanceof RouteListActivity)
                ((RouteListActivity) context).setDrawerClickTransition();

            switch (parent.getChildPosition(v))
            {
                case 0 :
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(context, v, context.getString(R.string.profile_transition));
                    Intent profileIntent = UserProfileActivity_.intent(context).get();
                    context.startActivity(profileIntent, options.toBundle());
                    break;
                case 1 :
                    ActivityOptionsCompat emergency =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(context, v, context.getString(R.string.emergency_transition));
                    Intent emergencyIntent = EmergencyActivity_.intent(context).get();
                    context.startActivity(emergencyIntent, emergency.toBundle());
                    break;
                case 2 :
                    DialogUtil.makePhoneDialog(context,R.string.dialog_phone_leader_title, R.string.dialog_phone_leader, "12345678");
                    break;
                case 3 :
                    InstructionsUtil.showOverlay(context);
                    break;
                case 4 :
                    Intent mainIntent = MainActivity_.intent(context).get();
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(mainIntent);
                    context.finish();
                    break;
                default:
                    Toast.makeText(v.getContext(), "Position: " + parent.getChildPosition(v) , Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }

    /**
     * Used for passing the list of options to the drawer, because of Android Annotations.
     * @param drawers a list of drawer information-ojects. Consisting of an icon-id and title-string.
     */
    public void setDrawers(List<Drawer> drawers){
        this.drawerInfo = drawers;
        this.notifyDataSetChanged();
    }
}
