package app.alhamad.helpmechat;

import android.content.Context;

public class TimeConvertor {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time, Context context) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }


        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return context.getString(R.string.just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return context.getString(R.string.minute_ago);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " " + context.getString(R.string.minutes_ago);

        } else if (diff < 90 * MINUTE_MILLIS) {
            return context.getString(R.string.hour_ago);
        } else if (diff / HOUR_MILLIS == 2) {
            return "2" + " " + context.getString(R.string.hours_ago);

        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " " + context.getString(R.string.hours_ago);
        } else if (diff < 48 * HOUR_MILLIS) {
            return context.getString(R.string.yesterday);
        } else {
            if (diff / DAY_MILLIS > 7 && diff / DAY_MILLIS < 14) {
                return context.getString(R.string.week_ago);
            } else if (diff / DAY_MILLIS > 14 && diff / DAY_MILLIS < 21) {
                return "2" + " " + context.getString(R.string.weeks_ago);
            } else if (diff / DAY_MILLIS > 21 && diff / DAY_MILLIS < 30) {
                return "3" + " " + context.getString(R.string.weeks_ago);
            } else if (diff / DAY_MILLIS > 30 && diff / DAY_MILLIS < 60) {
                return context.getString(R.string.month_ago);
            } else if (diff / DAY_MILLIS > 60 && diff / DAY_MILLIS < 90) {
                return "2" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 90 && diff / DAY_MILLIS < 120) {
                return "3" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 120 && diff / DAY_MILLIS < 150) {
                return "4" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 150 && diff / DAY_MILLIS < 180) {
                return "5" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 180 && diff / DAY_MILLIS < 210) {
                return "6" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 210 && diff / DAY_MILLIS < 240) {
                return "7" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 240 && diff / DAY_MILLIS < 270) {
                return "8" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 270 && diff / DAY_MILLIS < 300) {
                return "9" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 300 && diff / DAY_MILLIS < 330) {
                return "10" + " " + context.getString(R.string.months_ago);
            } else if (diff / DAY_MILLIS > 330 && diff / DAY_MILLIS < 365) {
                return "11" + " " + context.getString(R.string.months_ago);
            } else {
                return diff / DAY_MILLIS + " " + context.getString(R.string.days_ago);
            }
        }
    }

}