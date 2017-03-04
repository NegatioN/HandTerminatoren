package no.mesan.handterminator.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import no.mesan.handterminator.R;
import no.mesan.handterminator.model.db.Package;

/**
 * Created by marhag on 16.02.15.
 */
public class PackageListAdapter extends RecyclerView.Adapter<PackageListAdapter.PackageViewHolder> {

    Context context;

    private List<Package> packages;

    private LayoutInflater inflater;
    private PackageViewHolder packageHolder;


    public PackageListAdapter(Context context, List<Package> packages) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        Log.d("LIST3", packages.size() + "");


        this.packages = packages;
    }

    /**
     * onCreate for the adapter, inflates the layout and initializes the ViewHolder
     * @param parent Parent ViewGroup
     * @param viewType type of view to add to recycler-view.
     * @return
     */
    @Override
    public PackageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_package_list, parent, false);

        packageHolder = new PackageViewHolder(view);

        return packageHolder;
    }

    /**
     * Gives the viewholder on a position values
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(PackageViewHolder holder, int position) {

        Package current = packages.get(position);

        holder.packageNumber.setText(current.getKolli()+"");
        holder.packageWeight.setText(current.getWeight() + "");
        holder.packageHeight.setText(current.getHeight()+"");
        holder.packageWidth.setText(current.getWidth()+"");
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    @Override
    public long getItemId(int position) {
        return packages.get(position).hashCode();
    }


    /**
     * Delete package from the list
     * @param position position of package to be deleted
     */
    public void delete(int position) {
        packages.remove(position);
        notifyItemRemoved(position);
    }



    class PackageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView packageNumber, packageWeight, packageHeight, packageWidth;

        /**
         * initializes the text areas of the viewholder
         * @param itemView
         */
        public PackageViewHolder(final View itemView) {
            super(itemView);

            //taskIcon = (ImageView) itemView.findViewById(R.id.taskIcon);
            packageNumber = (TextView) itemView.findViewById(R.id.package_number);
            packageWeight = (TextView) itemView.findViewById(R.id.package_weight);
            packageHeight = (TextView) itemView.findViewById(R.id.package_height);
            packageWidth = (TextView) itemView.findViewById(R.id.package_width);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            RecyclerView parent = (RecyclerView) view.getParent();
//            Toast.makeText(view.getContext(), "Kolli: " + parent.getChildPosition(view), Toast.LENGTH_SHORT).show();
        }
    }


}
