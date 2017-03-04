/*
 * Copyright (C) 2014 I.C.N.H GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.mesan.handterminator.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import no.mesan.handterminator.R;
import no.mesan.handterminator.adapter.TaskListAdapter;
import no.mesan.handterminator.model.maps.Bounds;
import no.mesan.handterminator.util.AnimationUtil;

/**
 * A {@link android.support.v7.widget.RecyclerView} that provides reordering with drag&amp;drop.
 * Furthermore you have to provide stable ids {@link android.support.v7.widget.RecyclerView.Adapter#setHasStableIds(boolean)}}
 */
public class TaskRecyclerView extends RecyclerView {
    private static final String TAG = TaskRecyclerView.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;
    private static final int LINE_THICKNESS = 12;
    private static final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 50;
    private static final int INVALID_ID = -1;
    private static final int AUTOSCROLL_MARGIN = 510;

    private int activePointerId = INVALID_POINTER_ID;
    private int lastEventY, downX, downY, totalOffsetY;

    private BitmapDrawable hoverCell;
    private Rect hoverCellOriginalBounds, hoverCellCurrentBounds;

    private boolean cellIsMobile = false;
    private long mobileItemId = INVALID_ID;

    private int smoothScrollAmountAtEdge;
    private boolean usWaitingForScrollFinish;

    private boolean isAnimating;
    private boolean isEditing;
    private int editLimit;


    public TaskRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public TaskRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    public TaskRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        smoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);

        // detector for the long press in order to start the dragging
        final GestureDetector longPressGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent event) {
                Log.d(TAG, "Longpress detected");

                downX = (int) event.getX();
                downY = (int) event.getY();
                activePointerId = event.getPointerId(0);

                totalOffsetY = 0;
                View selectedView = findChildViewUnder(downX, downY);

                //Return of selectedview is null or selectedview belongs to a finished task or selectedview is the footer
                if (selectedView == null || selectedView.getAlpha() < 1 || selectedView.getId() == R.id.tasklist_footer)
                    return;

                selectedView.setClickable(false);
                selectedView.setVisibility(INVISIBLE);

                mobileItemId = getChildItemId(selectedView);
                hoverCell = getAndAddHoverView(selectedView);
                cellIsMobile = true;
            }
        });

        //
        final OnItemTouchListener itemTouchListener = new OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
                if (longPressGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return cellIsMobile;
                    default:
                        touchEventsEnded();
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent event) {
                handleMotionEvent(event);
            }
        };
        addOnItemTouchListener(itemTouchListener);
    }

    private void handleMotionEvent(MotionEvent event) {
        //Log.d(TAG, String.format("handleMotionEvent %s", event));

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(activePointerId);

                lastEventY = (int) event.getY(pointerIndex);
                int deltaY = lastEventY - downY;

                if (cellIsMobile) {
                    hoverCellCurrentBounds.offsetTo(0, hoverCellOriginalBounds.top + deltaY + totalOffsetY);
                    hoverCell.setBounds(hoverCellCurrentBounds);
                    invalidate();

                    handleCellSwitch();

                    handleMobileCellScroll();
                }
                break;
            case MotionEvent.ACTION_UP:
                touchEventsEnded();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    touchEventsEnded();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Creates the hover cell with the appropriate bitmap and of appropriate
     * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
     * single time an invalidate call is made.
     */
    private BitmapDrawable getAndAddHoverView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();

        Bitmap b = getBitmapWithBorder(v);
//        Bitmap b = getBitmapFromView(v);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        hoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        hoverCellCurrentBounds = new Rect(hoverCellOriginalBounds);

        drawable.setBounds(hoverCellCurrentBounds);

        return drawable;
    }

    /**
     * Draws a black border over the screenshot of the view passed in.
     */
    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        Rect rect = new Rect(-5, -LINE_THICKNESS, bitmap.getWidth(), bitmap.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setColor(Color.GRAY);

        can.drawBitmap(bitmap, 0, 0, null);
        can.drawRect(rect, paint);

        return bitmap;
    }

    /**
     * Returns a bitmap showing a screenshot of the view passed in.
     */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    /**
     * dispatchDraw gets invoked when all the child views are about to be drawn.
     * By overriding this method, the hover cell (BitmapDrawable) can be drawn
     * over the recyclerviews's items whenever the recyclerviews is redrawn.
     */
    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverCell != null) {
            hoverCell.draw(canvas);
        }
    }

    private boolean isSameView(View view1, View view2) {
        if (view1 == null || view2 == null) {
            return true;
        }
        return getChildPosition(view1) == getChildPosition(view2);
    }

    View previousChild;
    /**
     * This method determines whether the hover cell has been shifted far enough
     * to invoke a cell swap. If so, then the respective cell swap candidate is
     * determined and the data set is changed. Upon posting a notification of the
     * data set change, a layout is invoked to place the cells in the right place.
     */
    private void handleCellSwitch() {
        ViewHolder mobileViewHolder = findViewHolderForItemId(mobileItemId);
        View mobileView = mobileViewHolder != null ? mobileViewHolder.itemView : null;

        if (mobileView != null) {
            //Set EditLimit to the activetask position if this is first swap of the edit.
            if (!isEditing) {
                setEditing(true, getAdapter().getActivePosition());
            }

            View childViewUnder;
            int originalItem = getChildPosition(mobileView);
            int bottom =    hoverCellCurrentBounds.bottom;
            int top =       hoverCellCurrentBounds.top;
            int center =    hoverCellCurrentBounds.centerY();
            childViewUnder = findChildViewUnder(0, center);

            //Don't animate if the child under is the same as last time.
            //This causes flickering up and down
            if (previousChild == childViewUnder)
                return;

            if (!isSameView(childViewUnder, mobileView)) {
                int childCenter = (int) (childViewUnder.getY() + childViewUnder.getHeight() / 2);

                // check if bottom overlaps middle of child
                if (center < childCenter && bottom > childCenter ) {
                   //Log.e("MOVEMENT: ", "Down");
                }
                // check if top overlaps middle of child
                else if (center > childCenter && top < childCenter) {
                    //Log.e("MOVEMENT: ", "Up");
                }
                else
                    return;

                swapElements(originalItem, getChildPosition(childViewUnder));
                previousChild = childViewUnder;
                isAnimating = true;
            }
            else
                previousChild = null;
        }
    }


    /**
     * Swaps the the elements with the given indices.
     *
     * @param fromIndex the from-element index
     * @param toIndex   the to-element index
     */
    private void swapElements(int fromIndex, int toIndex) {
        //Checks to see if the swapping elements are next to each other
        //If not, the elements between them are swapped one by one
        if (toIndex < editLimit || fromIndex == toIndex)
            return;

        if (toIndex == getAdapter().getSize() && fromIndex == toIndex-1) {
            toIndex = toIndex - 1;
            swapElements(fromIndex, toIndex);
        }
        else if (fromIndex - toIndex > 1)
            toIndex = fromIndex - 1;
        else if (fromIndex - toIndex < -1)
            toIndex = fromIndex + 1;

//        Log.i(TAG, String.format("Swapping %d with %d", fromIndex, toIndex));
        TaskListAdapter adapter = getAdapter();
        adapter.swapElements(fromIndex, toIndex);
        adapter.notifyDataSetChanged(); //adapter.notifyItemMoved(fromIndex, toIndex);
    }

    /**
     * Resets all the appropriate fields to a default state while also animating
     * the hover cell back to its correct location.
     */
    private void touchEventsEnded() {
        previousChild = null;
        ViewHolder viewHolderForItemId = findViewHolderForItemId(mobileItemId);
        if (viewHolderForItemId == null) {
            return;
        }
        final View mobileView = viewHolderForItemId.itemView;
        if (cellIsMobile || usWaitingForScrollFinish) {
            cellIsMobile = false;
            usWaitingForScrollFinish = false;
            activePointerId = INVALID_POINTER_ID;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.
            if (getScrollState() != SCROLL_STATE_IDLE) {
                usWaitingForScrollFinish = true;
                return;
            }

            hoverCellCurrentBounds.offsetTo(mobileView.getLeft(), mobileView.getTop());

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds",
                    sBoundEvaluator, hoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mobileItemId = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    mobileView.setClickable(true);
                    hoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();

        } else {
            touchEventsCancelled();
        }

    }

    /**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its
     * final location when the user lifts his finger by modifying the
     * BitmapDrawable's bounds.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };

    /**
     * Resets all the appropriate fields to a default state.
     */
    private void touchEventsCancelled() {
        ViewHolder viewHolderForItemId = findViewHolderForItemId(mobileItemId);
        if (viewHolderForItemId == null) {
            return;
        }
        View mobileView = viewHolderForItemId.itemView;
        if (cellIsMobile) {
            mobileItemId = INVALID_ID;
            mobileView.setVisibility(VISIBLE);
            hoverCell = null;
            invalidate();
        }
        cellIsMobile = false;
        activePointerId = INVALID_POINTER_ID;
    }

    /**
     * Determines whether this recyclerview is in a scrolling state invoked
     * by the fact that the hover cell is out of the bounds of the recyclerview;
     */
    private void handleMobileCellScroll() {
        handleMobileCellScroll(hoverCellCurrentBounds);
    }

    /**
     * This method is in charge of determining if the hover cell is above/below or
     * left/right the bounds of the recyclerview. If so, the recyclerview does an appropriate
     * upward or downward smooth scroll so as to reveal new items.
     */
    public boolean handleMobileCellScroll(Rect r) {
        if (getLayoutManager().canScrollVertically()) {
            int offset = computeVerticalScrollOffset();
            int height = getHeight();
            int extent = computeVerticalScrollExtent();
            int range = computeVerticalScrollRange();
            int hoverViewTop = r.top;
            int hoverHeight = r.height();

            if (hoverViewTop <= 0 && offset > 0) {
//                Log.e(TAG, String.format("scrolling vertically by %d", -smoothScrollAmountAtEdge));
                scrollBy(0, -smoothScrollAmountAtEdge);
                return true;
            }

            if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
//                Log.e(TAG, String.format("scrolling vertically by %d", smoothScrollAmountAtEdge));
                scrollBy(0, smoothScrollAmountAtEdge);
                return true;
            }
        }

        return false;
    }


    //Not reorder methods

    @Override
    public void stopScroll()
    {
        try
        {
            super.stopScroll();
        }
        catch( NullPointerException exception )
        {
            /**
             *  The mLayout has been disposed of before the
             *  RecyclerView and this stops the application
             *  from crashing.
             */
        }
    }

    //Override getadapter to return a TaskListAdapter, so we don't need to cast every time
    @Override
    public TaskListAdapter getAdapter() {
        return (TaskListAdapter) super.getAdapter();
    }

    /**
     * Scroll the list to show the current active task
     * @param position position of view of the task to be shown
     */
    public void focusTask(int position) {
        View activeTask = getAdapter().getViewAt(position);

        smoothScrollBy(0, activeTask.getBottom() - AUTOSCROLL_MARGIN);
    }

    /**
     * Check if the list is in editMode (reordering) or not
     * @return returns the local variable isEditing
     */
    public boolean isEditing() {
        return isEditing;
    }

    /**
     * Indicate that the list is being rearranged (setting the isEditing bool)
     * Also set a new editLimit based on what task is currently active
     *
     * @param state activate(true) or deactivate(false) editmode
     * @param editLimit index of first row that can be reordered
     */
    public void setEditing(boolean state, int editLimit) {
        //Only set new editLimit if activating editmode
        if (state)
            this.editLimit = editLimit;
        setEditing(state);
    }

    /**
     * Indicate that the list is being rearranged (setting the isEditing bool)
     * Changing the floating action button to reflect that it now saves changes to the list.
     *
     * @param state activate(true) or deactivate(false) editmode
     */
    public void setEditing(boolean state) {
        if (state != isEditing) {
            ImageButton actionButton = (ImageButton) ((Activity)getContext()).findViewById(R.id.deliverButton);
            AnimationUtil.buttonDeliverAnimation(state, actionButton);
        }
        isEditing = state;
    }
}