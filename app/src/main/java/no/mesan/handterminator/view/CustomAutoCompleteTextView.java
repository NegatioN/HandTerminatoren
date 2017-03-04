package no.mesan.handterminator.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * @author Martin Hagen, Sondre Sparby Boge
 *
 *
 */
public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    // Count  - max allowed items shown at any time
    // Border - border around the dropdown-list
    private int DROPDOWN_LIST_COUNT = 2, DROPDOWN_LIST_BORDER = 45;

    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }
    public CustomAutoCompleteTextView(Context context, AttributeSet attrs)
    {
        super(context,attrs);
    }
    public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context,attrs,defStyle);
    }
    @Override
    public boolean enoughToFilter()
    {
        boolean isEnough=(getThreshold()<=this.getText().length());

        if(isEnough)
        {
            if(this.getAdapter()!=null)
            {
                int itemsCount=0;
                int matchIndex=0;
                String txt = this.getText().toString();
                for (int i=0; i< this.getAdapter().getCount();i++)
                {
                    String dat = (String)this.getAdapter().getItem(i);
                    if(dat.startsWith(txt))
                    {
                        itemsCount++;
                        matchIndex=i;
                    }
                }
                if(itemsCount == 1)
                {
                    if(((String)getAdapter().getItem(matchIndex)).equals(txt))
                    {
                        isEnough=false;
                    }

                }
            }
        }
        return isEnough;

    }

    @Override
    public void onFilterComplete(int count) {
        // Sets dropdown-height depending on the list-count
        setDropDownHeight((count > DROPDOWN_LIST_COUNT ? DROPDOWN_LIST_COUNT : count) * getHeight()
                + (count == 1 ? DROPDOWN_LIST_BORDER : DROPDOWN_LIST_COUNT * 50));

        super.onFilterComplete(count);
    }


}