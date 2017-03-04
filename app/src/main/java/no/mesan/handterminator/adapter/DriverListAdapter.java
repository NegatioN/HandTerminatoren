package no.mesan.handterminator.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.DeliveryActivity;
import no.mesan.handterminator.R;
import no.mesan.handterminator.model.Driver;
import no.mesan.handterminator.model.db.Package;

/**
 * @author Martin Hagen
 * List of all drivers close by.
 */
public class DriverListAdapter extends RecyclerView.Adapter<DriverListAdapter.DriverViewHolder> {

    Context context;

    private LayoutInflater inflater;
    private DriverViewHolder viewHolder;

    int activePos = -1;

    private List<Driver> drivers;


    public DriverListAdapter(Context context, List<Driver> driversList) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.drivers = driversList;
    }

    /**
     * onCreate for the adapter, inflates the layout and initializes the ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public DriverViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_driver_list, parent, false);

        viewHolder = new DriverViewHolder(view);

        return viewHolder;
    }

    /**
     * Gives the viewholder on a position values
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(DriverViewHolder holder, int position) {

        Driver current = drivers.get(position);

        holder.driverName.setText(current.getName() + "");
        holder.driverNumber.setText(current.getNumber() + "");
        holder.driverDistance.setText(current.getDistance() + "");

        int color = R.color.superWhite;
        if (activePos == position)
            color = R.color.softAccent;

        holder.itemView.findViewById(R.id.delivery_row_layout).setBackgroundResource(color);
    }
    public List<Driver> getList() {
        return drivers;
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    @Override
    public long getItemId(int position) {
        return drivers.get(position).hashCode();
    }

    public Driver getDriver(){
        return (activePos != -1)?drivers.get(activePos):null;
    }


    class DriverViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView driverName, driverNumber, driverDistance;

        /**
         * initializes the text areas of the viewholder
         * @param itemView
         */
        public DriverViewHolder(final View itemView) {
            super(itemView);

            driverName = (TextView) itemView.findViewById(R.id.driver_name);
            driverNumber = (TextView) itemView.findViewById(R.id.driver_number);
            driverDistance = (TextView) itemView.findViewById(R.id.driver_distance);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            activePos = getPosition();
            Log.d("Setter position: ", activePos + "");
            notifyDataSetChanged();
        }
    }
}
