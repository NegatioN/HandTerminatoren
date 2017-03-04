package no.mesan.handterminator.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import no.mesan.handterminator.DeliveryActivity;
import no.mesan.handterminator.R;
import no.mesan.handterminator.ScanningActivity;
import no.mesan.handterminator.listener.TaskTouchListener;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Package;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.util.AnimationUtil;
import no.mesan.handterminator.util.ImageUtil;
import no.mesan.handterminator.util.PrintUtil;


/**
 * @author Lars-Erik Kasin
 * Adapter for the recycler-views in scanningActivity's list of
 * Task-objects and Packages.
 */
public class ScanListAdapter extends RecyclerView.Adapter<ScanListAdapter.ScanListViewHolder> {

    Context context;

    private static final double SCANNING_LIMIT = 0.90;

    private static final int INVALID_POSITION = -1;

    //Unique itemId for the viewholder of the active task
    private long activeId;

    //For keeping track of currently active/expanded view
    private View expandedRow;
    private int expandedRowHeight;

    private LayoutInflater inflater;
    private ScanListViewHolder scanHolder;

    private ImageButton faButton;

    private View ripple;

    //Array of viewholders for easy external access to views in the list
//    ScanListViewHolder[] holders;

    List<Task> taskList = Collections.emptyList();

    public ScanListAdapter(Context context, ImageButton button, long selectedRouteId, View ripple) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.faButton = button;
        this.ripple = ripple;

        //gets us all deliveries from database. modify this query to get only from a certain route later
        DBRoute selectedRoute = DBRoute.findById(DBRoute.class, selectedRouteId);

        taskList = selectedRoute.getDeliveryTasks();
        Log.d("Tasks", taskList.toString());
    }


    @Override
    public ScanListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.row_scan_list, parent, false);

        scanHolder = new ScanListViewHolder(view);

        return scanHolder;
    }

    @Override
    public void onBindViewHolder(ScanListViewHolder holder, int position) {

        final Task currentTask = taskList.get(position);
        final int packages = currentTask.getPackages().size();
        final int scanned = countScans(currentTask);

        //Set up the packagelist inside this row
        holder.scanListTitle.setText(currentTask.getName());
        holder.scanListCount.setText(scanned + "/" + packages);
        holder.setExpandedHeight(currentTask.getPackages().size());

        holder.scanPackageList.setAdapter(getNewAdapter(currentTask));
        holder.scanPackageList.setLayoutManager(new LinearLayoutManager(context));

        //Set the right icon for the right type of task
        switch (currentTask.getType()) {
            case Task.TASK_DELIVERY:
                holder.scanListIcon.setImageResource(R.drawable.ic_delivery);
                break;
            case Task.TASK_PICKUP:
                holder.scanListIcon.setImageResource(R.drawable.ic_pickup);
                break;
            default:
                holder.scanListIcon.setImageResource(R.drawable.ic_delivery);
        }

        // Color the row of a delivery green if all packages are scanned or not
        if (scanned == packages)
            holder.itemView.findViewById(R.id.list_row_header).setBackgroundResource(R.color.scannedGreen);
        else
            holder.itemView.findViewById(R.id.list_row_header).setBackgroundResource(R.color.superWhite);

        //Collapse/expand recycled views off screen (Only show the active task)
        if (holder.itemView == expandedRow)
            holder.hideShowRow();
    }

    //Moved this out of onBindViewHolder to keep it clean. Can be moved back
    private RecyclerView.Adapter getNewAdapter(final Task currentTask){
        return new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = inflater.inflate(R.layout.row_scan_list_package, parent, false);

                return new ScanListViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                final Package currentPackage = currentTask.getPackages().get(position);

                if (position % 2 == 0)
                    holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.softBackground));

                TextView title = (TextView) holder.itemView.findViewById(R.id.scanListPackageTitle);
                ImageView check = (ImageView) holder.itemView.findViewById(R.id.scanListCheck);

                title.setText(currentPackage.getKolli() + "");
                check.setVisibility(currentPackage.isScannedIn() ? View.VISIBLE : View.INVISIBLE);

                holder.itemView.setOnTouchListener(new TaskTouchListener(context){
                    @Override
                    public void onLongClick() {
                        //dialog?
                        ((ScanningActivity) context).unscannPackage(currentPackage.getKolli() + "");
                    }
                });
            }

            @Override
            public int getItemCount() {
                return currentTask.getPackages().size();
            }
        };
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @Override
    public long getItemId(int position) {
        return taskList.get(position).hashCode();
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    //When a package is scanned, the deliverylist is update with this progress
    public boolean updateScannedPackage(String kolli) {
        for (Task task : taskList) {
            for(Package p : task.getPackages()) {
                if (p != null && p.getKolli().equals(kolli)) {
                    //Set the found package to scanned
                    if(p.isScannedIn()) {
                        // activity where it is displayed
                        Toast.makeText(context, "Kolli allerede scannet", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    //If you have scanned more than limit, show fab
                    if(getPackagesScanned() >= SCANNING_LIMIT){
                        if(AnimationUtil.inMiddle(ripple, faButton)) {
                            AnimationUtil.setStartPosCorner(faButton,context, ripple);
                        }
                        else
                            AnimationUtil.slideView(faButton,context);

                        faButton.setImageResource(ImageUtil.getButtonDrawable(getPackagesScanned()));
                        faButton.setVisibility(View.VISIBLE);
                    }
                    p.setScannedIn(true);
                    //Update the deliverylist
                    notifyDataSetChanged();
                    return true;
                }
            }
        }
        Toast.makeText(context, "Kolli finnes ikke", Toast.LENGTH_SHORT).show();
        return false;
    }

    //Count how many packages are scanned in a task. For displaying progress (e.g. 2/4)
    public int countScans(Task task) {
        int count = 0;
        for (Package p : task.getPackages())
            if (p.isScannedIn())
                count++;
        return count;
    }

    //Checks if all packages is scanned, must return true for moving to next screen
    public double getPackagesScanned()
    {
        double total = 0, scanned = 0;

        for(Task task : taskList)
        {
            for(Package p : task.getPackages())
            {
                if(p.isScannedIn())
                    scanned++;
                total++;
            }
        }
        return (scanned/total);
    }

    //Just for testing, scans all packages
    public void scanAll()
    {
        for(Task task: taskList)
        {
            for(Package p : task.getPackages())
                p.setScannedIn(true);
        }
        faButton.setImageResource(ImageUtil.getButtonDrawable(getPackagesScanned()));
        notifyDataSetChanged();
    }

    /**
     * Collapses the expanded view (Should only ever be a single one)
     *
     * @return returns a bool to check if any views were open
     */
    public boolean closeAll() {
        if (expandedRow == null)
            return false;

        scanHolder.animate(expandedRow);
        return true;
    }

    public Package getPackage(String kolli) {
        for(Task t : taskList) {
            for(Package p : t.getPackages()) {
                if(p.getKolli().equals(kolli.trim()))
                    return p;
            }
        }
        return null;
    }

    public boolean unscannPackage(String kolli)
    {
        Package p = getPackage(kolli);
        if(!p.isScannedIn())
            return false;
        getPackage(kolli).setScannedIn(false);
        notifyDataSetChanged();
        return true;
    }

    /**
     * Delete task from tasklist
     *
     * @param position position of task to be deleted
     */
    public void delete(int position) {
        taskList.remove(position);
        notifyItemRemoved(position);
    }

    public int getSize() {
        return taskList.size();
    }

    class ScanListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView scanListTitle, scanListCount;
        RecyclerView scanPackageList;
        ImageView scanListIcon;

        //Measurements for collapsed and expanded views for animating the expand transition
        private final int ORIGINALHEIGHT = 125, PACKAGEHEIGHT = 45, PADDING = (int)
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics()); //offset in dp

        private int expandedHeight = ORIGINALHEIGHT;

        public ScanListViewHolder(final View itemView) {
            super(itemView);

            scanListIcon = (ImageView) itemView.findViewById(R.id.scanListIcon);
            scanListTitle = (TextView) itemView.findViewById(R.id.scanListTitle);
            scanListCount = (TextView) itemView.findViewById(R.id.scanListCount);
            scanPackageList = (RecyclerView) itemView.findViewById(R.id.packageRecycler);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            activateView(view);
        }

        //Adjust the expanded height of the view by how many packages the delivery contains
        public void setExpandedHeight(int packageCount) {
            this.expandedHeight = ORIGINALHEIGHT + PADDING + PACKAGEHEIGHT * packageCount;
        }

        public void activateView(View view) {

            //Make sure the rows are unclickable mid animation
            if (view.getHeight() == ORIGINALHEIGHT || view.getHeight() == expandedHeight) {

                //Clicking a collapsed row
                if (expandedRow != view) {

                    //Close currently expanded row
                    if (expandedRow != null)
                        animate(expandedRow);

                    //Set currently expanded row. This is now the active task
                    expandedRow = view;
                    activeId = getItemId();
                }

                animate(view);
            }
        }

        //Expand/Collapse a row in the list off screen without animation
        public void hideShowRow() {
            itemView.getLayoutParams().height = (getItemId() == activeId) ? expandedHeight : ORIGINALHEIGHT;
        }

        //Animate Expand/Collapse of a row in the list
        public void animate(final View view) {
            ValueAnimator valueAnimator;

            //Collapse view if the height is more than the original height(expanded view), expand if not
            if (view.getHeight() > ORIGINALHEIGHT) {
                valueAnimator = ValueAnimator.ofInt(expandedRowHeight, ORIGINALHEIGHT);

                //Closing selected row means no row is expanded
                if (view == expandedRow)
                    expandedRow = null;
            } else {
                expandedRowHeight = expandedHeight;
                valueAnimator = ValueAnimator.ofInt(ORIGINALHEIGHT, expandedHeight);
                view.findViewById(R.id.scanListExpand).setVisibility(View.VISIBLE);
            }

            valueAnimator = AnimationUtil.taskExpandAnimator(valueAnimator,view);

            valueAnimator.start();

        }
    }


}
