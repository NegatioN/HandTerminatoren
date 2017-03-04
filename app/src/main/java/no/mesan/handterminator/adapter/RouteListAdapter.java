package no.mesan.handterminator.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import no.mesan.handterminator.R;
import no.mesan.handterminator.RouteListActivity;
import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Task;

/**
 * Created by lars-erikkasin on 24.03.15.
 */
public class RouteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private List<DBRoute> routeList;
    private RouteListActivity context;

    //Colors for accenting the selected route
    private int white, accent, finished;

    public RouteListAdapter(RouteListActivity context, List<DBRoute> routeList) {
        this.context = context;
        this.routeList = routeList;
        inflater = LayoutInflater.from(context);

        white = context.getResources().getColor(R.color.superWhite);
        accent = context.getResources().getColor(R.color.softAccent);
        finished = context.getResources().getColor(R.color.softGrey);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_route_list, parent, false);

        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final DBRoute currentRoute = routeList.get(position);

        //Get the current route's tasks
        List<Task> tasks = currentRoute.getTasks();

        //Background of each row. Changes color when selected
        View content = holder.itemView.findViewById(R.id.route_row_content);

        TextView title = (TextView) holder.itemView.findViewById(R.id.routeTitle);
        TextView deliveries = (TextView) holder.itemView.findViewById(R.id.deliverCount);
        TextView pickups = (TextView) holder.itemView.findViewById(R.id.pickupCount);
        ImageView icon = (ImageView) holder.itemView.findViewById(R.id.routeIcon);

        int deliveryCount = context.countDeliveries(tasks);

        //Fill out the fields in each row
        title.setText(currentRoute.getName());
        deliveries.setText( deliveryCount + "");
        pickups.setText(tasks.size() - deliveryCount + "");

        //choose icon depending on shift-type
        if(currentRoute.isDayShift())
            icon.setImageResource(R.drawable.ic_sun);
        else
            icon.setImageResource(R.drawable.ic_moon);


        //Reset the backgroundcolor of the selected row, and reset the others..
        if (currentRoute.getId() != context.getSelectedRouteId())
            content.setBackgroundColor(white);
        else
            content.setBackgroundColor(accent);

        // Reset background-color of finished routes
        if(currentRoute.isCompleted()) {
            if(currentRoute.getId() != context.getSelectedRouteId())
                content.setBackgroundColor(finished);
            else
                content.setBackgroundColor(accent);
        }


        //Clicks on the list shows the chosen route's details on screen
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle the clicked route in the activity
                context.routeListClicked(currentRoute);

                //Update the background color to show the currently selected view
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public void setRouteList(List<DBRoute> routeList){
        this.routeList = routeList;
    }
};
