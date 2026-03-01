package trainboy888;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {
    private static final Pattern TOKEN = Pattern.compile("(\\d+)([smhdw])");

    private DurationParser() {
    }

    public static long parseDurationMillis(String input) {
        String value = input.toLowerCase(Locale.ROOT).trim();
        if (value.equals("perm") || value.equals("permanent") || value.equals("forever")) {
            return -1;
        }

        Matcher matcher = TOKEN.matcher(value);
        long total = 0;
        int matchedChars = 0;

        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);
            matchedChars += matcher.group(0).length();

            switch (unit) {
                case 's':
                    total += amount * 1000L;
                    break;
                case 'm':
                    total += amount * 60_000L;
                    break;
                case 'h':
                    total += amount * 3_600_000L;
                    break;
                case 'd':
                    total += amount * 86_400_000L;
                    break;
                case 'w':
                    total += amount * 604_800_000L;
                    break;
                default:
                    return 0;
            }
        }

        if (matchedChars != value.length()) {
            return 0;
        }

        return total;
    }

    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "Permanent";
        }

        long seconds = millis / 1000L;
        long weeks = seconds / 604800;
        seconds %= 604800;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder builder = new StringBuilder();
        append(builder, weeks, "w");
        append(builder, days, "d");
        append(builder, hours, "h");
        append(builder, minutes, "m");
        append(builder, seconds, "s");
        return builder.length() == 0 ? "0s" : builder.toString().trim();
    }

    private static void append(StringBuilder builder, long amount, String unit) {
        if (amount > 0) {
            builder.append(amount).append(unit).append(' ');
        }
    }
}
