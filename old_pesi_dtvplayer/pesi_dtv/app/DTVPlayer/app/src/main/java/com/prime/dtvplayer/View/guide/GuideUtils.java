package com.prime.dtvplayer.View.guide;

import java.util.concurrent.TimeUnit;

public class GuideUtils {
    private static int sWidthPerHour = 0;

    public static void setWidthPerHour(int widthPerHour) {
        sWidthPerHour = widthPerHour;
    }

    private static int convertMillisToPixel(long millis) {
        return (int) (millis * sWidthPerHour / TimeUnit.HOURS.toMillis(1));
    }

    public static int convertMillisToPixel(long startMillis, long endMillis) {
        // Convert to pixels first to avoid accumulation of rounding errors.
        return GuideUtils.convertMillisToPixel(endMillis)
                - GuideUtils.convertMillisToPixel(startMillis);
    }

    static long convertPixelToMillis(int pixel) {
        return pixel * TimeUnit.HOURS.toMillis(1) / sWidthPerHour;
    }


}
