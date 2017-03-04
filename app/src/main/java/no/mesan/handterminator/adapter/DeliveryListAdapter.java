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

import no.mesan.handterminator.DeliveryActivity;
import no.mesan.handterminator.R;
import no.mesan.handterminator.ScanningActivity;
import no.mesan.handterminator.listener.TaskTouchListener;
import no.mesan.handterminator.model.db.Package;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.util.AnimationUtil;
import no.mesan.handterminator.util.ImageUtil;

/**
 * @author Martin Hagen
 * Adapter for the recycler-views in deliveryActivity's list of packages.
 */
public class DeliveryListAdapter extends RecyclerView.Adapter<DeliveryListAdapter.DeliveryViewHolder> {

    final Context context;

    private List<Package> packages;

    private LayoutInflater inflater;
    private DeliveryViewHolder packageHolder;


    public DeliveryListAdapter(Context context, List<Package> packages) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.packages = packages;
    }

    /**
     * onCreate for the adapter, inflates the layout and initializes the ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public DeliveryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_delivery_list, parent, false);

        packageHolder = new DeliveryViewHolder(view);

        return packageHolder;
    }

    /**
     * Gives the viewholder on a position values
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(DeliveryViewHolder holder, int position) {

        Package current = packages.get(position);

        holder.packageId.setText(current.getKolli() + "");

        int color = R.color.superWhite;
        if (packages.get(position).isScannedOut())
            color = R.color.scannedGreen;

        holder.itemView.findViewById(R.id.delivery_row_layout).setBackgroundResource(color);
    }

    //When a package is scanned, the packagelist is update with this progress
    public boolean updateScannedPackage(String kolli) {
        for(Package p : packages) {
            if (p != null && p.getKolli().equals(kolli)) {
                //Set the found package to scanned
                if(p.isScannedOut()) {
                    // activity where it is displayed
                    Toast.makeText(context, "Kolli allerede scannet", Toast.LENGTH_SHORT).show();
                    return false;
                }
                //Toast.makeText(context, "Scannet: " + kolli, Toast.LENGTH_SHORT).show();
                p.setScannedOut(true);
                //Update the deliverylist
                notifyDataSetChanged();
                //if all packages is scanned, start signature fragment
                if(isAllScanned())
                    ((DeliveryActivity)context).startSignature();
                return true;
            }
        }
        Toast.makeText(context, "Kolli finnes ikke", Toast.LENGTH_SHORT).show();
        return false;
    }

    public Package getPackage(String kolli) {
        for(Package p : packages) {
            if(p.getKolli().equals(kolli.trim()))
                return p;
        }
        return null;
    }

    public boolean unscannPackage(String kolli)
    {
        Package p = getPackage(kolli);
        if(!p.isScannedOut())
            return false;
        getPackage(kolli).setScannedOut(false);
        notifyDataSetChanged();
        return true;
    }

    public List<Package> getList() {
        return packages;
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    @Override
    public long getItemId(int position) {
        return packages.get(position).hashCode();
    }

    //Just for testing, scans all packages
    public void scanAll()
    {
        int i = 0;
        for(Package p : packages) {
            i++;
            p.setScannedOut(true);
        }
        notifyDataSetChanged();
    }

    /**
     * If the delivery is canceled(back pressed), unscann all packages
     * This is to avoid having already scanned out packages, eaven if they did not get delivered
     */
    public void unscanAll()
    {
        for(Package p : packages)
            unscannPackage(p.getKolli());
    }

    /**
     * checks if all packages is scanned
     */
    private boolean isAllScanned()
    {
        for(Package p : packages)
            if(!p.isScannedOut())
                return false;
        return true;
    }

    /**
     * Delete package from the list
     * @param position position of package to be deleted
     */
    public void delete(int position) {
        packages.remove(position);
        notifyItemRemoved(position);
    }

    public int countScanned()
    {
        int count = 0;
        for(Package p : packages)
            if(p.isScannedOut())
                count++;
        return count;
    }

    class DeliveryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView packageId;

        /**
         * initializes the text areas of the viewholder
         * @param itemView
         */
        public DeliveryViewHolder(final View itemView) {
            super(itemView);

            packageId = (TextView) itemView.findViewById(R.id.package_id);

            //itemView.setOnClickListener(this);
            itemView.setOnTouchListener(new TaskTouchListener(context){
                @Override
                public void onLongClick() {
                    //dialog?
                    ((DeliveryActivity) context).unscannPackage(packageId.getText()+"");
                }
            });
        }

        @Override
        public void onClick(View view) {

        }
    }
}
