package no.mesan.handterminator.util;

import no.mesan.handterminator.R;

/**
 *@author Martin Hagen
 * Util class for smaller more general methods
 */
public class ImageUtil {

    private static final double SCANNING_LIMIT = 0.90;

    /**
     * @param percent percent packages scanned.
     * @return correct drawable for actionbutton depending on percentage scanned packages.
     */
    public static int getButtonDrawable(double percent)
    {
        if(percent >= SCANNING_LIMIT)
            return R.drawable.ic_arrow_forward_white_48dp;
        else
            return R.drawable.ic_check;
    }
}
