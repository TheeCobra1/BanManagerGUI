package BMG.banManagerGUI.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhdwMy])");
    
    public static long parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }
        
        long totalMillis = 0;
        Matcher matcher = TIME_PATTERN.matcher(timeString);
        
        while (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "s":
                    totalMillis += amount * 1000L;
                    break;
                case "m":
                    totalMillis += amount * 60000L;
                    break;
                case "h":
                    totalMillis += amount * 3600000L;
                    break;
                case "d":
                    totalMillis += amount * 86400000L;
                    break;
                case "w":
                    totalMillis += amount * 604800000L;
                    break;
                case "M":
                    totalMillis += amount * 2592000000L;
                    break;
                case "y":
                    totalMillis += amount * 31536000000L;
                    break;
            }
        }
        
        return totalMillis;
    }
    
    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "Permanent";
        }
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;
        
        if (years > 0) {
            return years + " year" + (years == 1 ? "" : "s");
        } else if (months > 0) {
            return months + " month" + (months == 1 ? "" : "s");
        } else if (weeks > 0) {
            return weeks + " week" + (weeks == 1 ? "" : "s");
        } else if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s");
        } else if (hours > 0) {
            return hours + " hour" + (hours == 1 ? "" : "s");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }
    }
    
    public static long getDurationMillis(String duration) {
        switch (duration.toLowerCase()) {
            case "1 hour":
                return 3600000L;
            case "1 day":
                return 86400000L;
            case "1 week":
                return 604800000L;
            case "1 month":
                return 2592000000L;
            case "3 months":
                return 7776000000L;
            case "6 months":
                return 15552000000L;
            default:
                return parseTime(duration);
        }
    }
    
    public static String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        
        if (diff < 1000) {
            return "just now";
        }
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;
        
        if (years > 0) {
            return years + " year" + (years == 1 ? "" : "s") + " ago";
        } else if (months > 0) {
            return months + " month" + (months == 1 ? "" : "s") + " ago";
        } else if (weeks > 0) {
            return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        } else if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else {
            return seconds + " second" + (seconds == 1 ? "" : "s") + " ago";
        }
    }
}