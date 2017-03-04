package no.mesan.handterminator.adapter;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import no.mesan.handterminator.CardActivity_;
import no.mesan.handterminator.NavDrawerActivity;
import no.mesan.handterminator.R;
import no.mesan.handterminator.listener.TaskTouchListener;
import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.DBRoute;
import no.mesan.handterminator.model.db.Statistics;
import no.mesan.handterminator.model.db.Task;
import no.mesan.handterminator.model.db.User;
import no.mesan.handterminator.util.AnimationUtil;
import no.mesan.handterminator.util.DialogUtil;
import no.mesan.handterminator.util.RouteUtil;

/**
 * Created by lars-erikkasin on 02.02.15.
 */
public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    Context context;
    private User currentUser = User.findById(User.class,1L);

    private static final int INVALID_POSITION = -1;
    private static final int VIEW_TYPE_STANDARD = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    //Unique itemId for the viewholder of the active task
    //private long activeId;
    private Task activeTask;

    //For keeping track of currently active/expanded view
    private View expandedRow;
    private View footer;

    private LayoutInflater inflater;
    private TaskViewHolder taskHolder;

    private boolean animating;

    //Array of viewholders for easy external access to views in the list
    TaskViewHolder[] holders;

    List<Task> taskList = Collections.emptyList();

    public TaskListAdapter(Context context, Route route) {
        this.context = context;

        inflater = LayoutInflater.from(context);

        taskList = route.getTasks();
        
        //Deactivate all tasks, to make sure the correct one is activated
        activateTask(null);

        estimateDeliveries(route, false);

        holders = new TaskViewHolder[taskList.size()];
    }

    @Override
    public int getItemViewType(int position) {
        if (position == taskList.size())
            return VIEW_TYPE_FOOTER;

        return VIEW_TYPE_STANDARD;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_FOOTER) {
            view = inflater.inflate(R.layout.row_task_list_footer, parent, false);
            footer = view;
        }
        else
            view = inflater.inflate(R.layout.row_task_list, parent, false);

        taskHolder = new TaskViewHolder(view);

        return taskHolder;
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        if (position == taskList.size()) {
            return;
        }

        final Task currentTask = taskList.get(position);

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");

        holder.taskText.setText(currentTask.getName());
        holder.taskEta.setText(dateFormat.format(currentTask.getEta()));
        holder.taskRecipiant.setText(currentTask.getReceiver().getName());
        holder.taskAddress.setText(currentTask.getAddress());
        holder.taskZip.setText(currentTask.getZip());
        holder.taskCity.setText(currentTask.getCity());
        holder.taskPackages.setText(String.valueOf(currentTask.getPackages().size()));

        //onClickListener for calling directly from the list
        holder.callCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.makePhoneDialog(context, R.string.dialog_phone_title, R.string.dialog_phone_customer_positive, currentTask.getReceiver().getPhone());
            }
        });

        //Set the right icon for the right type of task
        switch (currentTask.getType()) {
            case Task.TASK_DELIVERY:
                holder.taskIcon.setImageResource(R.drawable.ic_delivery);
                break;
            case Task.TASK_PICKUP:
                holder.taskIcon.setImageResource(R.drawable.ic_pickup);
                break;
            default:
                holder.taskIcon.setImageResource(R.drawable.ic_delivery);
        }



        //Construct the viewHolder array (but no need to overwrite every time)
        if (holders[position] == null)
            holders[position] = holder;

        //Collapse/expand recycled views off screen (Only show the active task)
        if (currentTask.isActive())
            holder.quickExpand();
        else
            holder.quickCollapse();

        //Collapse/expand recycled views off screen (Only show the active task)
        if (currentTask.isFinished()) {
            holder.itemView.setAlpha(0.5f);
            holder.quickCollapse();
        }
        else
            holder.itemView.setAlpha((1f));
    }

    @Override
    public int getItemCount() {
        return taskList.size()+1;
    }

    @Override
    public long getItemId(int position) {
        if (position == taskList.size())
            return 0;
        return taskList.get(position).hashCode();
    }

    public View getViewAt(int position){
        return holders[position].itemView;
    }

    /**
     * Swaps the elements of the data list when the RecyclerView is reordered
     *
     * @param fromIndex the index
     * @param toIndex   the index
     */
    public void swapElements(int fromIndex, int toIndex) {
        Task temp = taskList.get(fromIndex);
        taskList.set(fromIndex, taskList.get(toIndex));
        taskList.set(toIndex, temp);

        TaskViewHolder tempHolder = holders[fromIndex];
        holders[fromIndex] = holders[toIndex];
        holders[toIndex] = tempHolder;
    }

    /**
     * Estimate the deliverytime of each task, and update the values in the recyclerview.
     * @param route the current route (often after reorganizing)
     * @param delivery true for delivery, false for retrieval of package.
     */
    public void estimateDeliveries(Route route, boolean delivery) {
        RouteUtil.estimateDeliveries(route, getActivePosition(), delivery);

        //Update the values in the recyclerview
        for(int i = getActivePosition()-1; i < taskList.size(); i++)
            notifyItemChanged(i);
    }


    /**
     * Get the position of the current active task
     *
     * @return returns the position of the active task
     */
    public int getActivePosition() {
        if (activeTask != null)
            return taskList.indexOf(activeTask);
        return INVALID_POSITION;
    }


    /**
     * Sets a new active task and updates(expands) it's view
     *
     * @param position position of the new active task
     * @return return the new active view
     */
    public int updateActive(int position) {
        if (position >= taskList.size()) {
            activateTask(null);
            closeAll();
            return position;
        }

        if (position >= 0 && getTask(position) != null) {

            //If this task is finished, skip it.
            if (getTask(position).isFinished()) {
                return updateActive(++position);
            }

            activateTask(getTask(position));
            TaskViewHolder holder = holders[position];
            if (holder.itemView != expandedRow)
                holder.activateView(position);
            return position;
        }
        return INVALID_POSITION;
    }

    /**
     * Sets the active task to finished and it's view is faded out.
     */
    public void finishTask(int position) {
        if (position < 0 || position >= taskList.size())
            return;

        Task task = getTask(position);

        if (task != null) {
            task.setFinished(true);
            //update statistics for user and task-delivery
            updateTaskStatistics(task);
        }

        //Finish route if last item
        if (position == (taskList.size()-1)) {
            task.getDbRoute().setCompleted(true);
            task.getDbRoute().setStatus("Fullført");
        }
        else
            task.getDbRoute().setStatus("Påbegynt");
    }


    /**
     * Collapses the expanded view (Should only ever be a single one)
     *
     * @return returns a bool to check if any views were open
     */
    public boolean closeAll() {
        if (expandedRow == null)
            return false;

        taskHolder.animate(expandedRow);
        return true;
    }


    public Task getTask(int pos){
        return taskList.get(pos);
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

    public void addTask(){
        holders = new TaskViewHolder[taskList.size()];
        activeTask.setActive(false);
        notifyDataSetChanged();
    }

    public int getSize() {
        return taskList.size();
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    public void updateTimeFrame(Task task){
        TaskViewHolder holder = holders[taskList.indexOf(task)];
        holder.taskTime.setText(task.getTimeSlotStartString() + task.getTimeSlotEndString());
    }

    public void activateTask(Task task){
        if (activeTask !=  null) {
            activeTask.setActive(false);
        }

        if (task == null)
            for (Task t: taskList)
                t.setActive(false);
        else
            task.setActive(true);

        activeTask = task;
    }

    /** ********************* **
     *        VIEWHOLDER       *
     ** ********************* **/
    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView taskText, taskRecipiant, taskAddress, taskZip, taskCity, taskPackages, taskTime, taskEta;
        ImageView taskIcon, callCustomer;
        View taskExpand;

        //Measurements for collapsed and expanded views for animating the expand transition
        private final int ORIGINALHEIGHT = 125, EXPANDEDHEIGHT = 333;

        public TaskViewHolder(final View itemView) {
            super(itemView);

            taskIcon = (ImageView) itemView.findViewById(R.id.taskIcon);
            taskText = (TextView) itemView.findViewById(R.id.taskText);
            taskAddress = (TextView) itemView.findViewById(R.id.taskAddress);
            taskRecipiant = (TextView) itemView.findViewById(R.id.taskRecipiant);
            taskZip = (TextView) itemView.findViewById(R.id.taskZip);
            taskCity = (TextView) itemView.findViewById(R.id.taskCity);
            taskPackages = (TextView) itemView.findViewById(R.id.taskPackages);
            taskTime = (TextView) itemView.findViewById(R.id.taskTimeFrame);
            taskEta = (TextView) itemView.findViewById(R.id.taskEta);
            taskExpand = itemView.findViewById(R.id.taskExpand);
            callCustomer = (ImageView) itemView.findViewById(R.id.taskCall);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //show details for chosen view
            if (getPosition() != taskList.size())
                openTaskDetails(itemView);
        }


        /**
         * Opens the details view for the given task with animation
         *
         * @param view view of the chosen task
         */
        public void openTaskDetails(View view) {
            if (animating)
                return;

            setAnimating(true);

            // gets task from list
            long taskId = taskList.get(getPosition()).getId();
            Activity activity = (Activity) context;

            //shared element animation
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, "details");

            Intent intent = new Intent(activity, CardActivity_.class);
            intent.putExtra("taskId", taskId);
            ActivityCompat.startActivity(activity, intent, options.toBundle());
        }

        /**
         * Expands the selected view in the taskList, and collapses all others.
         * @param position
         */
        public void activateView(int position) {
            //Never expand finished tasks
//            if(taskList.get(position).isFinished())
//                return;

            View view = holders[position].itemView;
            //Make sure the rows are unclickable mid animation
            if (view.getHeight() == ORIGINALHEIGHT || view.getHeight() == EXPANDEDHEIGHT) {

                //Clicking a collapsed row
                if (expandedRow != view) {

                    //Close currently expanded row
                    if (expandedRow != null)
                        animate(expandedRow);

                    //Set currently expanded row. This is now the active task
                    expandedRow = view;
                }

                animate(view);
            }
        }

        //Expand/Collapse a row in the list off screen without animation
        public void quickExpand() {
            itemView.findViewById(R.id.taskExpand).setVisibility(View.VISIBLE);
            itemView.getLayoutParams().height = EXPANDEDHEIGHT;
        }

        //Expand/Collapse a row in the list off screen without animation
        public void quickCollapse() {
            itemView.getLayoutParams().height = ORIGINALHEIGHT;
        }

        //Animate Expand/Collapse of a row in the list
        public void animate(final View view) {
            ValueAnimator valueAnimator;

            //Collapse view if the height is more than the original height(expanded view), expand if not
            if (view.getHeight() > ORIGINALHEIGHT) {

                valueAnimator = ValueAnimator.ofInt(EXPANDEDHEIGHT, ORIGINALHEIGHT);

                //Closing selected row means no row is expanded
                if (view == expandedRow)
                    expandedRow = null;
            }
            else {
                valueAnimator = ValueAnimator.ofInt(ORIGINALHEIGHT, EXPANDEDHEIGHT);
                view.findViewById(R.id.taskExpand).setVisibility(View.VISIBLE);
            }

            valueAnimator = AnimationUtil.taskExpandAnimator(valueAnimator,view);
            valueAnimator.start();

        }
    }

    /**
     * After each package delivered, calculates money earned etc
     * @param currentTask newly finished task
     */
    //TODO currently calculates from "estimated time", not real time spent
    private void updateTaskStatistics(Task currentTask){
        Statistics userStats = currentUser.getStatistics();
        double secondsSpent = currentTask.getTime() + (NavDrawerActivity.PAUSE_OFFSET * 60);
        double hoursSpent = secondsSpent / 3600D;
        int numPackages = currentTask.getSize();


        double hourlySalary = Statistics.calculateHourlyMoney(currentUser.getHourSalary(), hoursSpent);
        double packageSalary = Statistics.calculatePackageMoney(currentUser.getPackageSalary(), numPackages);
        double drivingReimbursed = Statistics.reimburseDriving((int)currentTask.getDistance());
        //money-related statistics
        userStats.addHourSalaryEarned(hourlySalary);
        userStats.addPackageSalarayEarned(packageSalary);
        userStats.addDrivingReimbursed(drivingReimbursed);


        //Non money-related statistics
        userStats.addMetersDriven(currentTask.getDistance());
        userStats.addTimeSpent((long)secondsSpent);
        userStats.addPackagesDelivered(numPackages);

    }


    //Dead methods?


    /**
     * Returns the first unfinished task in the tasklist. So that the driver doesn't have to
     * refinish finished tasks.
     * @return
     */
    public int getFirstUnfinished(){
        for (Task t : taskList)
            if (!t.isFinished()) {
                activateTask(t);
                return taskList.indexOf(t);
            }
        return -1;
    }

    public View getFooter(){
        return footer;
    }

    public boolean isAnimating() {
        return animating;
    }

}
